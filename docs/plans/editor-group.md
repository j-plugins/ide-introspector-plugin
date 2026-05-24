# `editor.*` group — `editor.set_caret` + `editor.get_state`

## Purpose & motivation

JetBrains' built-in MCP server (IntelliJ 2025.2+) exposes file I/O (`open_file_in_editor`,
`replace_text_in_file`) but offers **no caret control** and **no full editor-state read**:
an agent can put a file on the screen but cannot move the cursor, see selection ranges,
inspect folded regions, count gutter markers, or know what's actually visible in the
viewport. The plugin's existing `psi.list_open_files` already reports a single
`caretOffset` for the focused tab — `editor.*` is the natural home for the richer
write-side (`set_caret`) and read-side (`get_state`) of the same data.

Why a **new** group rather than extending `psi.*`: the `psi.*` tools deliberately stay
read-only and PSI-tree-centric. Caret mutation is a write, and editor-viewport / folding /
inlay / gutter data are Swing-editor concerns that have nothing to do with PSI. Mixing
them in `psi.*` would muddle the group's contract.

What we deliberately do NOT add (already covered by JetBrains' MCP server):
- `editor.open_file` — use JetBrains `open_file_in_editor`.
- `editor.replace_text` — use JetBrains `replace_text_in_file` / `replace_specific_text`.
- `editor.close_file` — JetBrains has `close_file`.
- `editor.get_text` — JetBrains has `get_file_text_by_path`.

Success criterion: an agent can read the active file, navigate to a specific
line+column, scroll it into view, then re-query `get_state` to confirm where the caret
landed, what's selected, and which gutter markers (errors/warnings) are visible —
without a screenshot round-trip.

## Tool specifications

### `editor.set_caret`

```kotlin
@McpTool(name = "editor.set_caret")
@McpDescription(
    """
    |Moves the primary caret to a target position in an open editor and (by default)
    |scrolls it into view. Idempotent and reversible — returns the previous offset so the
    |caller can restore it.
    |
    |Use this when:
    |  - You're about to call psi.get_references / psi.find_usages with scope="at_offset"
    |    and want the IDE's caret to match the position you're querying (so the user sees
    |    what the agent is looking at).
    |  - You need to position the caret before invoking a context-sensitive action
    |    (Goto Declaration, Show Intentions) through ui.invoke_action_on.
    |  - You want to lead the user's eye to a specific line you just analyzed.
    |
    |Do NOT use this when:
    |  - The file isn't open yet — this tool fails fast with a "file not open" error.
    |    Call JetBrains' `open_file_in_editor` first, then `editor.set_caret`.
    |  - You want to select a range — this only moves the primary caret; selection is not
    |    yet a write tool in this group (file an issue if you need it).
    |  - You only need to read where the caret IS — call `editor.get_state` instead.
    |
    |Positioning: pass either `offset` (0-based document offset, like `psi.*`) or
    |`line`+`column` (both 1-based, IDE-conventional). If both are supplied, `offset` wins.
    |Out-of-range line/column are clamped to the file end, and the response's `clamped`
    |flag is set so the caller knows.
    |
    |Multiple editors per file (split view): the tool targets the editor returned by
    |`FileEditorManager.selectedEditor` for that file — i.e. the most recently focused
    |split. There is no way to address a specific split through this tool today.
    |
    |Returns: { ok, fileUrl, oldOffset, newOffset, line, column, madeVisible, clamped }.
    |Save `oldOffset` to undo with a follow-up `editor.set_caret` call.
    |
    |Examples:
    |  fileUrl=null, line=42, column=8                    — move caret in active tab to row 42, col 8
    |  fileUrl="file:///…/Foo.kt", offset=1024            — explicit file + byte offset
    |  line=42, scrollToVisible=false                     — move but don't scroll
    """
)
suspend fun editor_set_caret(
    @McpDescription("VFS URL of the file (from psi.list_open_files.url). null → use the active editor tab.")
    fileUrl: String? = null,
    @McpDescription("0-based document offset. Alternative to line+column. Wins if both supplied.")
    offset: Int? = null,
    @McpDescription("1-based line number. Used when `offset` is null.")
    line: Int? = null,
    @McpDescription("1-based column number. Used with `line`. Default 1.")
    column: Int = 1,
    @McpDescription("Scroll the editor so the caret is visible (ScrollType.MAKE_VISIBLE). Default true.")
    scrollToVisible: Boolean = true,
): SetCaretResponse
```

### `editor.get_state`

```kotlin
@McpTool(name = "editor.get_state")
@McpDescription(
    """
    |Returns the full state of an open editor in one call: primary caret + all secondary
    |carets, current selection, the logical line range currently visible in the viewport,
    |collapsed fold regions, inlay-hint counts by kind, and the gutter markers (errors,
    |warnings, infos) the daemon has highlighted so far.
    |
    |Use this when:
    |  - You need a snapshot of "what the user is looking at": viewport, caret, selection,
    |    visible errors — without resorting to a screenshot.
    |  - You're debugging a UI flow ("did my set_caret take? is the gutter marker now
    |    present?") and want a structured assertion.
    |  - You want a quick count of how bad the current file is (errors / warnings) before
    |    deciding whether to call psi.find_usages or read more code.
    |
    |Do NOT use this when:
    |  - The file isn't open — fails fast like `editor.set_caret`.
    |  - You need PSI structure — call psi.get_structure.
    |  - You need a pixel-accurate snapshot — call screenshot.capture / screenshot.region.
    |
    |Tunable: heavy sections are opt-in/opt-out.
    |  - `includeMultipleCarets=true` (default) — also enumerates secondary carets.
    |  - `includeFolding=true` (default) — collapsed regions and their placeholders.
    |  - `includeInlays=false` (default) — inlay hints can saturate the EDT on large
    |    files; off by default. Only counts (by kind) are returned even when on.
    |  - `gutterMinSeverity="WARNING"` (default) — filter daemon highlights by severity.
    |    Values: "ERROR", "WARNING", "WEAK_WARNING", "INFO", "ALL".
    |
    |Multiple editors per file (split view): same rule as `editor.set_caret` — we use
    |`FileEditorManager.selectedEditor` for that file (last focused split).
    |
    |Daemon timing: gutter markers come from `DaemonCodeAnalyzerImpl.getHighlights` and
    |only contain what the daemon has finished computing. On a file that was just opened
    |the list may be empty even though Problems-View will eventually populate; rerun the
    |tool after a short delay or call psi.* to surface structural problems immediately.
    |
    |Returns: rich `EditorState` — see plan doc for the data class shape.
    |
    |Examples:
    |  fileUrl=null                                            — full state of the active tab
    |  fileUrl="file:///…/Foo.kt", includeInlays=true          — also include inlay counts
    |  gutterMinSeverity="ERROR"                               — only errors in the gutter list
    """
)
suspend fun editor_get_state(
    @McpDescription("VFS URL of the file. null → active editor tab.")
    fileUrl: String? = null,
    @McpDescription("Enumerate secondary carets in `carets[]`. Default true.")
    includeMultipleCarets: Boolean = true,
    @McpDescription("Include `foldedRanges[]` (collapsed regions). Default true.")
    includeFolding: Boolean = true,
    @McpDescription("Include `inlayCounts` (count by kind — inline / block / after-line-end). Default false; can be expensive on large files.")
    includeInlays: Boolean = false,
    @McpDescription("Minimum daemon severity to include in `gutterMarkers[]`. One of ERROR / WARNING / WEAK_WARNING / INFO / ALL. Default WARNING.")
    gutterMinSeverity: String = "WARNING",
): EditorState
```

## Args + response models

`model/args/EditorArgs.kt` — `@Serializable` mirror types if used by Inspector (the
toolset can also pass primitives straight through):

```kotlin
@Serializable data class SetCaretArgs(
    val fileUrl: String?, val offset: Int?, val line: Int?, val column: Int = 1,
    val scrollToVisible: Boolean = true,
)
@Serializable data class GetStateArgs(
    val fileUrl: String?, val includeMultipleCarets: Boolean = true,
    val includeFolding: Boolean = true, val includeInlays: Boolean = false,
    val gutterMinSeverity: String = "WARNING",
)
```

`model/EditorInfo.kt` — responses:

```kotlin
@Serializable data class SetCaretResponse(
    val ok: Boolean,
    val fileUrl: String,
    val oldOffset: Int,
    val newOffset: Int,
    val line: Int,           // 1-based, post-clamp
    val column: Int,         // 1-based, post-clamp
    val madeVisible: Boolean,
    val clamped: Boolean,    // true if requested line/column was past EOF/EOL
)

@Serializable data class EditorState(
    val fileUrl: String,
    val carets: List<CaretInfo>,                  // primary first
    val selection: SelectionInfo?,                // null when no selection
    val visibleRange: LineRange?,                 // 1-based logical lines
    val foldedRanges: List<FoldInfo>?,            // null when includeFolding=false
    val inlayCounts: InlayCountSummary?,          // null when includeInlays=false
    val gutterMarkers: List<GutterMarkerInfo>?,   // null on error / unavailable
)
@Serializable data class CaretInfo(
    val offset: Int, val line: Int, val column: Int, val isPrimary: Boolean,
    val selectionStart: Int? = null, val selectionEnd: Int? = null,
)
@Serializable data class SelectionInfo(
    val start: Int, val end: Int, val startLine: Int, val endLine: Int, val length: Int,
)
@Serializable data class LineRange(val startLine: Int, val endLine: Int)  // 1-based inclusive
@Serializable data class FoldInfo(
    val startOffset: Int, val endOffset: Int,
    val startLine: Int, val endLine: Int,
    val placeholder: String, val expanded: Boolean,
)
@Serializable data class InlayCountSummary(
    val inline: Int, val block: Int, val afterLineEnd: Int, val total: Int,
)
@Serializable data class GutterMarkerInfo(
    val line: Int, val severity: String, val description: String?, val toolId: String?,
)
```

## IntelliJ APIs used

- `com.intellij.openapi.fileEditor.FileEditorManager` — `selectedTextEditor`,
  `selectedEditor`, `getEditors(VirtualFile)`.
- `com.intellij.openapi.editor.Editor` —
  `caretModel.{primaryCaret, allCarets, moveToLogicalPosition, moveToOffset, offset}`,
  `selectionModel.{selectionStart, selectionEnd, hasSelection}`,
  `scrollingModel.{scrollToCaret(ScrollType.MAKE_VISIBLE), visibleAreaOnScrollingFinished}`,
  `foldingModel.allFoldRegions`,
  `inlayModel.{getInlineElementsInRange, getBlockElementsInRange, getAfterLineEndElementsInRange}`.
- `com.intellij.openapi.editor.LogicalPosition` (line+column → offset).
- `com.intellij.openapi.editor.ScrollType.MAKE_VISIBLE`.
- `com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerImpl.getHighlights(Document, HighlightSeverity, Project)` —
  internal API (`@ApiStatus.Internal`); same approach the bundled "Problems" view uses.
- `com.intellij.lang.annotation.HighlightSeverity` — severity enum to filter.
- `com.intellij.openapi.application.ReadAction.compute` / `runReadAction` for daemon /
  document reads inside the EDT block (folding model also requires a read action when
  enumerated outside the EDT).

Stability note: `DaemonCodeAnalyzerImpl` is `@ApiStatus.Internal`. If it disappears,
fall back to walking `MarkupModel.allHighlighters` filtered by the daemon's
`HighlightInfo` tooltip renderer marker — slightly fuzzier but stable.

## Threading & EDT model

- Both tools touch a Swing `Editor` → must run inside `onEdtBlocking { … }`
  (uses `ModalityState.any()`).
- Inside the EDT block, wrap PSI / daemon / document reads in
  `ReadAction.compute<T, RuntimeException> { … }`. (EDT does not imply read-action
  ownership — daemon read can throw if you skip this.)
- `editor.set_caret` is a write to the caret model, which is also EDT-only — same
  bounce.
- No PSI parsing required. `editor.get_state` does not call `psi.*` machinery.

## Timeout strategy

- Hard 10 s cap (CLAUDE.md). Both tools complete in <100 ms in practice on a normal
  file: caret math is O(1), fold/inlay/caret enumeration is O(N) over typically <100
  elements, daemon `getHighlights` is an in-memory list lookup.
- The one risk is `includeInlays=true` on a heavily annotated file (Rider, big
  Kotlin file with hint plugins) where inlay enumeration over the whole document can
  blow past 10k entries. We mitigate by returning *only counts*, not the elements
  themselves, and by leaving `includeInlays` off by default.

## Edge cases

1. **File not open.** `set_caret` and `get_state` fail fast with `McpExpectedError`
   `"File not open: <url>. Call open_file_in_editor first."`. Rationale: keeps the
   tools minimal, makes the open-vs-move distinction explicit, avoids duplicating
   JetBrains' `open_file_in_editor` semantics (which already handles "open or focus").
2. **`fileUrl=null` but no active tab.** Same `McpExpectedError` shape as `psi.*`:
   `"No active editor tab. Open a file first, or pass fileUrl from psi.list_open_files."`
3. **Line / column past EOF.** Clamp to the last line; clamp column to that line's
   end. Set `clamped=true` on the response so the caller knows it wasn't a no-op.
4. **`offset` past document end.** Clamp to `document.textLength`, `clamped=true`.
5. **Both `offset` and `line`+`column` passed.** `offset` wins; warn nothing but
   document this in `@McpDescription`.
6. **Multiple editors per file (split view).** Use
   `FileEditorManager.selectedEditor` for that file — the most recently focused split.
   If we ever want to target a specific split, that's a v2.
7. **Binary file in editor (image viewer, Hex Editor).** `selectedTextEditor` returns
   null even though the tab is "open". Fail with
   `"File is not a text editor: <url> (file type=<X>)"`.
8. **Daemon hasn't run yet (file just opened, indexing).** `gutterMarkers` is an
   empty list — not null. Caller distinguishes "no markers" from "couldn't query".
9. **Dumb mode.** Daemon highlights are limited but available; we don't gate on
   `DumbService`. Folding model and caret are dumb-safe.
10. **Multiple-caret edge: secondary carets with their own selection.** Each
    `CaretInfo` reports its own `selectionStart`/`selectionEnd`; the top-level
    `selection` reflects the primary caret's selection only.
11. **Folding model not yet populated** (lazy on first paint). `allFoldRegions` can
    return an empty array on a file that hasn't been displayed yet. Acceptable;
    document in tool description.

## META-INF wiring

`editor.*` works in **any** IDE — no Java module, no Kotlin plugin required. Register
in the existing `mcp-integration.xml` alongside the other always-on toolsets. No new
shim XML is needed.

Add one line to `src/main/resources/META-INF/mcp-integration.xml`:

```xml
<mcpServer.mcpToolset implementation="com.github.xepozz.ide.introspector.tools.EditorToolset"/>
```

## Files to create/modify

| Path | Op | What |
|------|----|------|
| `src/main/kotlin/com/github/xepozz/ide/introspector/tools/EditorToolset.kt` | Create | `@McpTool` methods (`editor_set_caret`, `editor_get_state`) — thin wrappers, EDT bounce, arg validation. |
| `src/main/kotlin/com/github/xepozz/ide/introspector/core/EditorStateInspector.kt` | Create | Headless logic: editor resolution, caret math, folding/inlay/daemon collection. Unit-testable. |
| `src/main/kotlin/com/github/xepozz/ide/introspector/model/EditorInfo.kt` | Create | `EditorState`, `CaretInfo`, `SelectionInfo`, `LineRange`, `FoldInfo`, `InlayCountSummary`, `GutterMarkerInfo`, `SetCaretResponse`. |
| `src/main/kotlin/com/github/xepozz/ide/introspector/model/args/EditorArgs.kt` | Create | `SetCaretArgs`, `GetStateArgs` (if Inspector needs them; primitives also fine). |
| `src/main/resources/META-INF/mcp-integration.xml` | Edit | Register `EditorToolset`. |
| `src/test/kotlin/.../core/EditorStateInspectorTest.kt` | Create | Unit: clamp logic, severity-string parsing, fold-info ordering, response building from fakes. |
| `src/test/kotlin/.../core/platform/EditorStateInspectorPlatformTest.kt` | Create | Platform: open fixture file, exercise `set_caret` and `get_state` against a real `EditorImpl`. |

## Test plan

**Unit (`EditorStateInspectorTest.kt`)** — no IntelliJ runtime:
- `gutterMinSeverity` parser: "ERROR" → HighlightSeverity.ERROR, "ALL" → INFORMATION
  floor, invalid → IllegalArgumentException.
- Line/column clamp helper: line > lineCount clamps to lineCount, column > lineEnd
  clamps to lineEnd, `clamped=true` returned.
- Inlay count summary aggregation: given fake lists, sums to `total`.

**Platform (`EditorStateInspectorPlatformTest.kt`)** — extends
`BasePlatformTestCase`, fixture file in `src/test/testData/editor/`:

1. **`set_caret` happy path.** Open `Sample.java` (with a public class on line 3),
   call `set_caret(line=3, column=14)` → assert `caretModel.logicalPosition == (2,13)`,
   `oldOffset != newOffset`, `clamped=false`.
2. **`set_caret` clamp.** Same file, call `set_caret(line=9999, column=1)` →
   `clamped=true`, caret at last line, no exception.
3. **`set_caret` file-not-open.** Use a `VirtualFile` not opened in the editor →
   expect `McpExpectedError("File not open: …")`.
4. **`get_state` after `set_caret`.** Move to (5,1), call `get_state()`, assert
   `carets[0].line == 5`, `selection == null`, `visibleRange` brackets line 5.
5. **`get_state` with selection.** Programmatically set selection via
   `editor.selectionModel.setSelection(…)`, call `get_state()` → `selection.length`
   matches.
6. **`get_state` with folding.** Insert a fold region via `foldingModel.runBatchFoldingOperation`,
   call `get_state(includeFolding=true)` → `foldedRanges` has the region with
   `expanded=false`.
7. **`get_state` gutter markers.** Use a fixture file with an obvious error
   (`int x = "bad";` in Java), wait for daemon
   (`DaemonCodeAnalyzer.getInstance(project).restart(); UIUtil.dispatchAllInvocationEvents()`
   loop with a 5 s timeout), assert at least one `gutterMarkers[]` entry with
   `severity="ERROR"`.
8. **Binary file rejection.** Open an image in the editor (or any file whose
   `selectedTextEditor` is null), expect `McpExpectedError("File is not a text editor…")`.

## Estimated effort

~1 day combined.

- Models + args: ~1 h.
- `EditorStateInspector` (caret math, fold/inlay collection, daemon read): ~3 h.
- `EditorToolset` wrappers + EDT bouncing + arg validation: ~1.5 h.
- META-INF wiring + smoke test in `runIde`: ~30 min.
- Unit tests: ~1 h.
- Platform tests (daemon timing is the fiddly part): ~2 h.

## Open questions / risks

1. **`set_caret` accepting `offset` as well as line+column?** Recommend **yes** —
   matches the `psi.*` precedent (`psi.get_references` / `psi.find_usages` accept
   either). Already in the signature above.
2. **`get_state` returning the current line's text content?** Recommend **yes, but
   guarded** — add a `includeCurrentLineText: Boolean = false` arg in v1.1; trivial
   to implement (`document.getText(lineStartOffset..lineEndOffset)`). Skipping in
   v1 keeps the response shape small and avoids embedding text the agent likely
   already has from `psi.get_structure`.
3. **Should we add `editor.scroll_to(line)` as a third tool?** Cheap (one
   `scrollingModel.scrollTo(LogicalPosition(line-1, 0), ScrollType.CENTER)` call), but
   `set_caret(scrollToVisible=true)` already covers the common case. Defer to a
   user-requested follow-up so we don't bloat the group prematurely.
4. **`DaemonCodeAnalyzerImpl` is `@ApiStatus.Internal`.** If JetBrains breaks it in a
   future EAP, swap to `MarkupModelEx.processRangeHighlightersOverlappingWith` filtered
   by daemon-owned tooltip renderer. Document a fallback path in the Inspector.
5. **Split-view targeting (v2).** Today we pick `selectedEditor`. If users want
   per-split control, add an optional `splitIndex: Int?` arg later — would require
   `FileEditorManagerEx.windows[…]` enumeration.
6. **Caret model events / undo.** `set_caret` does not generate undoable actions
   (caret moves aren't undoable in IntelliJ either — Ctrl-Z reverses edits, not
   caret moves). The "reversible" claim in the description means "the caller can
   restore via a second tool call using `oldOffset`", not "Ctrl-Z reverses it".

## References

- Existing similar code: `PsiToolset.caretOffsetOf` and `PsiToolset.resolveOffset` in
  `/home/user/ide-introspector-plugin/src/main/kotlin/com/github/xepozz/ide/introspector/tools/PsiToolset.kt`
  — same EDT-bounce + offset-resolution pattern.
- `EdtHelpers.onEdtBlocking` in
  `/home/user/ide-introspector-plugin/src/main/kotlin/com/github/xepozz/ide/introspector/util/EdtHelpers.kt`
  — the 10 s EDT helper.
- IntelliJ Community source: `platform/platform-impl/src/com/intellij/openapi/editor/impl/CaretModelImpl.kt`,
  `platform/analysis-impl/src/com/intellij/codeInsight/daemon/impl/DaemonCodeAnalyzerImpl.java`.
- JetBrains MCP server (built-in): `open_file_in_editor`, `replace_text_in_file`,
  `get_open_in_editor_file_text`. None expose caret control or folding/inlay/gutter
  state — confirms the gap this group fills.
