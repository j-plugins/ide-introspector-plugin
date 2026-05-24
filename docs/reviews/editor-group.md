# Review: editor.set_caret + editor.get_state (`editor.*` group)

Branch: `claude/project-features-analysis-odEwP` @ HEAD (sole feature
commit `0c0bdb1`). Scope: `core/EditorStateInspector.kt`,
`model/EditorInfo.kt`, `model/args/EditorArgs.kt`,
`tools/EditorToolset.kt`, `META-INF/mcp-integration.xml` registration.
**No tests** (`src/test/.../core/EditorStateInspectorTest.kt` and the
platform fixture promised in the plan are missing).

## Verdict
Needs changes before merge. Shape is right and matches the plan
(EDT-bounce via `onEdtBlocking`, fail-fast `McpExpectedError` on
file-not-open, severity parsing, 5-section `@McpDescription`), but
`setCaretAtLineColumn` has two correctness bugs (line clamp boundary is
off-by-one, post-move logical position can disagree with `newOffset`
because we pass the *un-clamped* column to `moveToLogicalPosition`), the
`visibleRange` block reads `editor.scrollingModel.visibleArea` and calls
`xyToLogicalPosition` *outside* any read action while inside the EDT
block — fine for the Swing call but the entire `captureState` body sits
on the EDT yet only the inlay + daemon sub-calls take a read action,
and the test suite the plan calls out (8 platform tests including
daemon-marker fixture) is absent. Plus the `MarkupModelEx` fallback for
gutter markers double-counts because daemon results already live in the
same markup model.

## Top finding
**Finding 1 (HIGH)** — `setCaretAtLineColumn` reports a `(line, column)`
in the response that's read from `caretModel.logicalPosition` *after*
the caret has been moved with the un-clamped `targetCol`. For a
`column=999` request on a 10-char line, the caller gets back
`column=999` (virtual-space logical position) but `newOffset = lineEnd`.
The two fields disagree silently and `clamped=true` is the only signal —
worse, a caller round-tripping `set_caret(line, column)` →
`set_caret(offset)` lands somewhere different than expected.

## Summary
- Four source files (`EditorStateInspector.kt` 363 LOC, `EditorInfo.kt`
  128 LOC, `EditorArgs.kt` 22 LOC, `EditorToolset.kt` 189 LOC).
- One `mcp-integration.xml` line added next to the other always-on
  toolsets — correct, `editor.*` does not depend on the Java / Kotlin
  modules per plan.
- Both `@McpDescription` strings match the plan verbatim (5-section
  convention: what / use / don't use / returns / examples), use
  trim-margin.
- Hard rules: 10 s cap via `onEdtBlocking` (no custom timeout).
  `kotlinx-serialization-json` policy untouched. EDT bounce uses
  `ModalityState.any()` via the existing helper.
- No tests at all — `src/test/.../core/EditorStateInspectorTest.kt` /
  `EditorStateInspectorPlatformTest.kt` from the plan don't exist.
- `docs/MCP_TOOLS.md` is regenerated and lists both tools.

## Findings

### 1. [HIGH] `setCaretAtLineColumn` reports a logical column from the un-clamped move — `column` field can be virtual-space past EOL

`EditorStateInspector.kt:121-156`:
```kotlin
val effectiveOffset = (lineStart + targetCol).coerceAtMost(lineEnd).coerceAtLeast(0)
val oldOffset = editor.caretModel.offset
editor.caretModel.moveToLogicalPosition(LogicalPosition(effectiveLineIdx, targetCol.coerceAtLeast(0)))
…
val pos = editor.caretModel.logicalPosition
…
return SetCaretResponse(
    …
    newOffset = effectiveOffset,        // clamped to lineEnd
    line = pos.line + 1,
    column = pos.column + 1,            // post-move virtual column
    …
)
```

`moveToLogicalPosition(LogicalPosition(line, col))` with `col` past the
line end places the caret at the line end (offset-wise) but reports a
*virtual* `logicalPosition.column == requested-col` because
`EditorImpl` virtualizes columns past EOL when virtual-space is on (and
even without virtual-space, the caret offset is clamped to EOL while
the column field reflects the logical position). Result: caller sees
`newOffset = 14`, `column = 999`, `clamped = true`. Three fields and
they don't agree.

The fix is to pass the clamped logical position to `moveToLogicalPosition`:

```kotlin
val effectiveCol = (effectiveOffset - lineStart).coerceAtLeast(0)
editor.caretModel.moveToLogicalPosition(LogicalPosition(effectiveLineIdx, effectiveCol))
```

…and then `pos.column + 1` will match `newOffset`. (Even better: use
`moveToOffset(effectiveOffset)` which guarantees the two stay in sync.)

### 2. [HIGH] `MarkupModelEx` fallback for gutter markers reads from the *editor*-local markup model — wrong scope and double-counts when daemon is up

`EditorStateInspector.kt:328-340`:
```kotlin
val markup = (editor.markupModel as? MarkupModelEx)
```

`editor.markupModel` is the **editor-local** markup model (per-editor
RangeHighlighters, e.g. bookmarks, breakpoint badges, search results).
The daemon's `HighlightInfo`s live in the **document** markup model
(`DocumentMarkupModel.forDocument(document, project, false)`), which is
shared across all editors for that document. As written:

1. When the primary `DaemonCodeAnalyzerImpl.getHighlights` path
   succeeds (the common case), the fallback never runs.
2. When it throws, we silently scan the wrong markup model and return
   highlights that are not the gutter markers the caller asked for
   (more likely to be empty than to contain the daemon's errors).

Fix: replace `editor.markupModel` with
`DocumentMarkupModel.forDocument(document, project, false) as? MarkupModelEx`.
This is what the `@ApiStatus.Internal` `DaemonCodeAnalyzerImpl.getHighlights`
itself reads from internally, and is the recommended public path per
JetBrains support (see Sources). Also note `HighlightInfo` lives in
`daemon.impl`, but `fromRangeHighlighter` / `getSeverity` /
`getDescription` / `getInspectionToolId` / `getActualStartOffset` are
not annotated `@ApiStatus.Internal` individually — the import is
acceptable as long as you stay on those methods.

### 3. [HIGH] No tests — the plan called for 3 unit tests + 8 platform tests including a daemon-markers fixture

`src/test/kotlin/.../core/EditorStateInspectorTest.kt` and
`src/test/kotlin/.../core/platform/EditorStateInspectorPlatformTest.kt`
don't exist (`find /home/user/ide-introspector-plugin/src/test -name
"*Editor*"` returns nothing). The plan (test plan section) listed:

- Unit: `parseSeverity` mapping, line/column clamp helper, inlay-count
  aggregation. All three are testable without the IDE.
- Platform: 8 cases (happy path, clamp, file-not-open, get_state after
  set, get_state with selection, with folding, gutter markers with
  daemon round-trip, binary rejection).

Bug Finding 1 and Finding 2 would both be caught by case 1 + case 7.
Bug Finding 5 below would be caught by case 4.

### 4. [MED] `editor.virtualFile` access is nullable — empty `fileUrl=""` slips through both response paths

`EditorStateInspector.kt:104` (in `setCaretAtOffset`) and `:172` (in
`captureState`):
```kotlin
val url = editor.virtualFile?.url ?: ""
```

For a non-file-backed editor (light virtual file, scratch buffer,
console), `editor.virtualFile` can legitimately be null. The current
code returns `fileUrl=""` in the response — caller has no way to know
they were operating on a virtual editor with no VFS path. Since
`resolveEditor` requires a real `VirtualFile`, in practice this only
fires when someone bypasses `resolveEditor` (and Phase 2 might), but
it's a latent foot-gun. Either:
- Make the response field nullable (`fileUrl: String?`), or
- `require(editor.virtualFile != null)` early so we never get here.

### 5. [MED] `selection` reports the *primary* caret's selection only — but the plan said `selection` is "primary caret's selection" and `carets[i].selectionStart/End` covers secondary carets. Implementation matches the plan, BUT the docstring on `EditorState.selection` says "Primary caret's selection" while the description on the tool says "current selection" (singular, no clarification). Caller with multi-caret + secondary-only selection sees `selection=null` and might assume no selection exists at all.

`EditorToolset.kt:107-117` — the `editor.get_state` `@McpDescription`
mentions "current selection" without saying "primary only". Multi-caret
selections show up only in `carets[i]`. Doc bug, not a code bug;
amend the `@McpDescription` to say "primary caret's selection — see
`carets[i].selectionStart/End` for secondary-caret selections".

### 6. [MED] `selectionInfo` `length = end - start` lies for column-mode block selection

`EditorStateInspector.kt:215` — `length = end - start`. For a column /
block selection (`editor.selectionModel.blockSelectionStarts/Ends`),
`selectionStart`/`selectionEnd` span the bounding box (top-left to
bottom-right offset) which makes `length` overstate the selected
character count. Not a v1 blocker, but document it or detect
`isBlockSelection` and report differently.

### 7. [MED] `visibleRange` end-line uses `visibleArea.y + visibleArea.height` — off-by-one at the bottom

`EditorStateInspector.kt:226-228`:
```kotlin
xyToLogicalPosition(java.awt.Point(visibleArea.x, visibleArea.y + visibleArea.height))
```

The point at `y + height` is one pixel below the visible area (the
visible rect is `[y, y+height)`). For an editor showing exactly lines
10–30, this can report `endLine=31` because the point falls on the
next logical row. Use `y + height - 1` (clamp at 0). Cosmetic but the
field is one of the load-bearing snapshot values; agents will assert
on it.

### 8. [MED] `foldedRanges` returns ALL fold regions, not collapsed ones — name + docstring say "collapsed"

`EditorStateInspector.kt:240` — `editor.foldingModel.allFoldRegions.map
{ … expanded = region.isExpanded … }`. The `EditorInfo.kt:43` docstring
says "Collapsed and expanded fold regions" (already drifted from the
plan which said "collapsed regions"), and the `@McpDescription` at
`EditorToolset.kt:128` says "collapsed regions and placeholders". Two
choices:
1. Filter to `region.isExpanded == false` (matches the description).
2. Keep both but make the description honest — "all fold regions; check
   `expanded` to distinguish collapsed from expanded".

Recommend (2) since "collapsed only" loses information that's cheap to
include. Update both descriptions to match.

### 9. [MED] `parseSeverity` is called twice — once before EDT bounce (line 158) for validation, then again inside `captureState` (line 279)

`EditorToolset.kt:158`:
```kotlin
EditorStateInspector.parseSeverity(gutterMinSeverity)   // discarded
```
`EditorStateInspector.kt:279`:
```kotlin
minSeverity = parseSeverity(gutterMinSeverityName),
```

Functionally fine — `parseSeverity` is pure — but it's wasted work and
suggests the contract is unclear. `captureState` should take a
`HighlightSeverity` (already parsed), not a string. Then the toolset's
single call is the validation gate. Removes the string-coupling
between the two layers.

### 10. [MED] `runRead` helper duplicates `EdtHelpers.readActionBlocking` semantics inline — diverges from project pattern

`EditorStateInspector.kt:353-362` — local hand-rolled `runRead` using
`arrayOfNulls<Any?>(1)` instead of using
`ApplicationManager.getApplication().runReadAction<T, Throwable> { … }`
(see `EdtHelpers.kt:57`) or `ReadAction.compute<T, RuntimeException> { … }`.
Functionally fine but inconsistent with the rest of the codebase and
hides the unchecked-throwable swallow behind a generic cast. Replace
with `ReadAction.compute<T, RuntimeException> { block() }` (which is
what the rest of the plan implied) or the existing `runReadAction<T, Throwable>`.

### 11. [LOW] `editor.virtualFile` is on `Editor` since 2022.2 — minor API target check

The plugin targets recent IntelliJ; the property is available. Just a
note in case anyone backports — the older path was
`FileDocumentManager.getInstance().getFile(editor.document)`.

### 12. [LOW] `setCaretAtOffset` doesn't preserve secondary carets — silently collapses multi-caret mode

`EditorStateInspector.kt:99`:
```kotlin
editor.caretModel.moveToOffset(newOffset)
```

`CaretModel.moveToOffset` operates on the primary caret only; secondary
carets are preserved (matching the plan's "Multi-caret
preservation/clear semantics" item — the plan said "primary moves,
secondaries untouched"). Verified by reading `CaretModelImpl`. So the
implementation is correct here. **Item is OK** — flagged so the
checklist gets a real answer rather than "didn't look".

### 13. [LOW] `editor.set_caret` `@McpDescription` says `scrollToVisible` uses `ScrollType.MAKE_VISIBLE` — true, but doesn't mention the call is async (viewport may not actually be on-screen when the response returns)

`EditorInfo.kt:13-14` does note this on `madeVisible`, but the
`@McpDescription` itself ("Scroll the editor so the caret is visible")
implies sync. `scrollingModel.scrollToCaret` is typically synchronous
(no animation when called from EDT outside the smooth-scroll path)
but doesn't *guarantee* it. Cheap doc clarification.

### 14. [LOW] `setCaretAtLineColumn` `clampedColumn` uses `> lineEnd` not `>= lineEnd`

`EditorStateInspector.kt:137` — `clampedColumn = lineStart + targetCol > lineEnd`.
Request `column = lineLength + 1` (one past the last character): `lineStart + targetCol == lineEnd`, so `clampedColumn=false`. But the caret in fact moves to the character *at* lineEnd (the newline / EOL boundary), which is the line end — many users would expect `clamped=true` here. Off-by-one ambiguity; either fix to `>=` or document that "column may equal line-length+1 (post-EOL boundary) without `clamped` being set".

### 15. [LOW] `EditorState.gutterMarkers` doc says "Always non-null on success" but the field is `List<GutterMarkerInfo>? = null`

`EditorInfo.kt:50` — the field is nullable in the model but always
populated. Make it `List<GutterMarkerInfo> = emptyList()` (non-null
default) to align with the docstring's invariant, OR document when it
*can* be null (currently: never).

### 16. [LOW] Five distinct sentinel exception classes for `resolveEditor` — could be one sealed class with a `type` field

`EditorStateInspector.kt:37-46` — `FileNotOpenException`,
`NotATextEditorException`, `NoActiveEditorException`,
`FileNotFoundException`. Caller (`EditorToolset.kt:86-96`,
duplicated for `:162-172`) catches all four with identical handling.
Sealed class + `when` would let the toolset write one `catch (e:
ResolutionError)` and the duplicated try/catch block would collapse.

### 17. [LOW] `requireProject()` raises an `McpExpectedError` with `JsonObject(emptyMap())` — pattern repeated across the four catch arms; helper would clean it up

`EditorToolset.kt:86-96` and `:162-172` — every catch arm is the same
shape: `throw McpExpectedError(e.message ?: "<fallback>", JsonObject(emptyMap()))`.
Extract a `private fun expected(message: String): Nothing = throw McpExpectedError(message, JsonObject(emptyMap()))`.

## Threading & EDT model — mostly OK, one nitpick

- Toolset wraps in `onEdtBlocking` (10 s, `ModalityState.any()` via
  `EdtHelpers`). Correct per project pattern.
- Inspector's `captureState` runs on EDT (read access is implicit per
  the threading model docs — JetBrains write-intent lock on EDT).
  Inlay + daemon reads further take a read action via `runRead`. This
  is belt-and-suspenders correct.
- `FoldingModel.allFoldRegions` accessed directly on EDT — fine, the
  data structure is editor-local and the EDT call path is the one the
  platform uses itself.
- `xyToLogicalPosition` is EDT-only; in EDT block, fine.
- `scrollingModel.scrollToCaret(MAKE_VISIBLE)` is EDT-only; in EDT
  block, fine.
- `DaemonCodeAnalyzerImpl.getHighlights(document, severity, project)`
  is `@ApiStatus.Internal` — implementation acknowledges this in a
  comment and has a fallback. Plan called out the same. OK.

## Timeouts — OK
Single `onEdtBlocking` per tool, default 10 s. No nested timeouts. The
plan's risk on `includeInlays=true` over heavy files is mitigated by
returning counts only.

## `@McpDescription` quality — good, two drifts
- `editor.get_state` says "current selection" without clarifying it's
  the primary caret's (Finding 5).
- `editor.get_state` describes `foldedRanges` as "collapsed regions"
  but the implementation returns all regions (Finding 8).

Both descriptions otherwise faithfully follow the plan's 5-section
template and use trim-margin (KSP-processor friendly).

## Internal-API leakage in EditorInfo — none
Response data classes import only:
- `kotlinx.serialization.Serializable` — fine.

No `HighlightInfo`, `MarkupModelEx`, `DaemonCodeAnalyzerImpl` symbols
leak into the wire format. Severity is serialized as a `String` (the
`.name`), not the `HighlightSeverity` object. Clean.

## Test coverage — absent

No unit tests, no platform tests. The plan's test plan listed 11
specific cases; zero of them exist. This alone is merge-blocking by
the project's testing criteria (`docs/CODE_QUALITY.md`).

The three unit-testable helpers — `parseSeverity`, line/column clamp,
inlay-count aggregation — don't need the IDE runtime and could land
this week. The 8 platform tests need `BasePlatformTestCase` (pattern
used by the existing `PsiStructureWalkerPlatformTest`) and a fixture
file under `src/test/testData/editor/`. Estimated effort matches the
plan's ~2 h figure.

## Recommended actions before merge

1. **Finding 1** — pass clamped column to `moveToLogicalPosition` (or
   switch to `moveToOffset(effectiveOffset)` for `setCaretAtLineColumn`).
   Add platform test that asserts `column = pos.column + 1 ==
   (newOffset - lineStart) + 1` for a clamped move.
2. **Finding 2** — switch the `MarkupModelEx` fallback to read from
   `DocumentMarkupModel.forDocument(document, project, false)`. Add
   platform test that fakes a `DaemonCodeAnalyzerImpl` throw and
   verifies the fallback returns the same highlights.
3. **Finding 3** — add the missing test files. Minimum: 3 unit tests
   for `parseSeverity` / clamp / inlay aggregation, and platform tests
   for cases 1, 3, 7 (the three that touch the bugs above).
4. **Finding 8** — pick either filtering or honest doc, and amend.
5. **Finding 5** — clarify "primary caret" in `editor.get_state`
   description.
6. Findings 4, 6, 7, 9–10 — straightforward cleanups.
7. Findings 11–17 — nits.

## File / line references
- `core/EditorStateInspector.kt:121-156` — clamped-column bug (Finding 1)
- `core/EditorStateInspector.kt:328-340` — wrong markup model (Finding 2)
- `core/EditorStateInspector.kt:104,172` — nullable `virtualFile.url ?: ""` (Finding 4)
- `core/EditorStateInspector.kt:215` — `length = end - start` (Finding 6)
- `core/EditorStateInspector.kt:225-228` — `visibleRange` off-by-one (Finding 7)
- `core/EditorStateInspector.kt:239-254` — all vs collapsed folds (Finding 8)
- `core/EditorStateInspector.kt:158,279` — `parseSeverity` twice (Finding 9)
- `core/EditorStateInspector.kt:353-362` — local `runRead` (Finding 10)
- `core/EditorStateInspector.kt:99` — `moveToOffset` preserves secondaries (Finding 12, OK)
- `core/EditorStateInspector.kt:137` — `> lineEnd` boundary (Finding 14)
- `tools/EditorToolset.kt:107-117` — `@McpDescription` "current selection" (Finding 5)
- `tools/EditorToolset.kt:128` — `@McpDescription` "collapsed" (Finding 8)
- `tools/EditorToolset.kt:71` — `scrollToVisible` async note (Finding 13)
- `tools/EditorToolset.kt:86-96, 162-172` — duplicate catch arms (Finding 17)
- `model/EditorInfo.kt:50` — nullable vs always-populated (Finding 15)
- `META-INF/mcp-integration.xml:11` — registration (OK)
- `src/test/.../EditorStateInspector*` — missing (Finding 3)

Sources consulted:
- Plan `docs/plans/editor-group.md`
- [DocumentMarkupModel.java (JetBrains/intellij-community)](https://github.com/JetBrains/intellij-community/blob/master/platform/editor-ui-ex/src/com/intellij/openapi/editor/impl/DocumentMarkupModel.java)
- [HighlightInfo.java (JetBrains/intellij-community)](https://github.com/JetBrains/intellij-community/blob/master/platform/analysis-impl/src/com/intellij/codeInsight/daemon/impl/HighlightInfo.java)
- [How To get current file problems (JetBrains support)](https://intellij-support.jetbrains.com/hc/en-us/community/posts/19378358826770-How-To-get-current-file-problems) — `DocumentMarkupModel.forDocument(document, project, true).getAllHighlighters()` is the recommended path
- [API for reporting/highlighting problems outside of inspections (JetBrains support)](https://intellij-support.jetbrains.com/hc/en-us/community/posts/13285107972114-API-for-reporting-highlighting-problems-outside-of-inspections)
- [Internal API Migration (IntelliJ Platform Plugin SDK)](https://plugins.jetbrains.com/docs/intellij/api-internal.html)
- [Threading Model (IntelliJ Platform Plugin SDK)](https://plugins.jetbrains.com/docs/intellij/threading-model.html) — EDT implicitly holds write-intent (read) lock
- [ScrollingModel (Unofficial API docs)](https://dploeger.github.io/intellij-api-doc/com/intellij/openapi/editor/ScrollingModel.html)
- [FoldingModel (Unofficial API docs)](https://dploeger.github.io/intellij-api-doc/com/intellij/openapi/editor/FoldingModel.html)
- [FileEditorManager.java (JetBrains/intellij-community)](https://github.com/JetBrains/intellij-community/blob/master/platform/analysis-api/src/com/intellij/openapi/fileEditor/FileEditorManager.java) — `selectedTextEditor` null on background thread or non-text selection
