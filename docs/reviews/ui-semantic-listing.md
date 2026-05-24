# Review: ui.list_tool_windows + ui.list_dialogs

Branch: `claude/project-features-analysis-odEwP` @ `733906d`
Reviewer scope: the two new `@McpTool`s plus their salvaged inspectors and models.

## Verdict
Needs changes. Compiles, registers, and returns sensible data for the happy paths,
but it ships visibly as "placeholder" code (model KDoc says so, inspector class
KDoc says so), and the published response shapes diverge from the plan's
contract in ways that will be load-bearing for agents and tests. Plug the
contract gaps + add the missing plugin-id attribution before declaring done.

## Summary
- No inline-vs-inspector duplication: the inlined version that landed in
  `0c0bdb1` already delegates to `ToolWindowInspector` / `DialogInspector`
  by FQN. The fixer commit (`733906d`) added the inspectors and models as
  "stubs to unblock compile". So the inlined methods are NOT dead code;
  they're the only entry points, and they correctly delegate inside
  `onEdtBlocking { … }`. The fixer comment about "minimal stubs" refers to the
  inspectors themselves, not to a duplicate.
- The implementations work but the contract drifted from the plan: response
  fields renamed (`isVisible`→`visible`, `isModal`→`modal`, dialog `id`→
  `componentId`, missing `isShowing`, `isFloating`), `providedByPluginId` is
  hard-coded `null`, `Bounds` is nullable, and there are zero tests.
- The plan's "Field Service" expectations (plugin attribution via the
  `com.intellij.toolWindow` EP, cached through `core/internal/ExtensionMetadata`)
  are silently dropped. Easy to wire in — the cache infrastructure exists.

## Findings (numbered, severity-tagged)

1. **[HIGH] `ToolWindowInspector.kt:48` — `providedByPluginId = null` always.**
   The plan's whole motivation for not just returning raw `ToolWindow` objects
   is to attribute each window to a plugin. The current code hard-codes `null`
   with a TODO-shaped KDoc but no follow-up. `core/internal/ExtensionMetadata.kt`
   plus the EP `com.intellij.toolWindow` (key = `id` attribute, value =
   `pluginDescriptor.pluginId.idString`) is the exact pattern. Remember
   CLAUDE.md pitfalls: use `ep.size()` + `ExtensionComponentAdapter.pluginDescriptor`
   (public field), never `extensionList`.

2. **[HIGH] `DialogInfo.kt:13,15` and `ToolWindowInfo.kt:14-25` — response
   shape doesn't match the plan.** Plan documented `isVisible`/`isActive`/
   `isSplit`/`isFloating`/`isModal`/`isResizable`/`isShowing`/`id`; impl
   shipped `visible`/`active`/`splitMode`/`modal`/`resizable`/`componentId`
   and dropped `isShowing` and `isFloating` entirely. `Bounds` is nullable but
   the plan promised non-null. `DialogInfo` is missing the `isShowing` flag the
   plan calls out as the user-visible filter result. Pick one and update the
   `@McpDescription` ("Returns: { dialogs: DialogInfo[]… }") to match — right
   now the description references field names the response doesn't actually
   carry.

3. **[HIGH] `DialogInspector.kt:27` — Always-on `registry.register(dlg)` leaks
   memory and pollutes `ComponentRegistry` even when there are no dialogs the
   agent will follow up on.** ComponentRegistry is fine for active UI nodes;
   for transient dialogs the `WeakHashMap`/`WeakReference` design copes, so
   correctness is OK, but no `ComponentRegistry.getInstance().compact()` is
   ever called in this flow. Minor — flag for a follow-up housekeeping pass
   once we have realistic call volume.

4. **[MEDIUM] `DialogInspector.kt:29` — `DialogWrapper.findInstance(dlg)`
   resolution is unreliable.** `findInstance(c)` walks UP `c.getParent()`
   until it finds a `DialogWrapperDialog`. For a `JDialog` that IS the
   `DialogWrapperDialog` (the common IntelliJ case) the very first iteration
   matches; but for any non-wrapper `Dialog` you get the fallback FQN. That's
   correct for our model but the plan's `@McpDescription` ("walks client-property
   hierarchy") is misleading. Either update the description or descend into the
   dialog's content pane first (`dlg.contentPane` or `dlg.rootPane`) so a
   wrapper whose dialog class is generic still resolves. The platform's own
   `findInstanceFromFocus()` uses the same upward walk, so the current
   behaviour matches platform convention — but document it accurately.

5. **[MEDIUM] `ToolWindowInspector.kt:19` — Focused-project resolution is too
   weak.** Plan: prefer `IdeFocusManager.getGlobalInstance().lastFocusedFrame?.project`,
   fall back to `ProjectManager.getInstance().openProjects.firstOrNull()`. Impl:
   only the fallback. With multiple open projects this picks the wrong one any
   time the user has switched focus.

6. **[MEDIUM] `ToolWindowInspector.kt:46` — `iconPath = tw.icon?.toString()`
   per plan should emit `null` for procedural icons. The current code emits the
   `toString()` (often `"jetbrains.icons.CachedImageIcon@…"`). Filter to
   `IconLoader`-backed `CachedImageIcon` and call its `getOriginalPath()` (or
   pattern-match on `toString()`), otherwise emit null.

7. **[MEDIUM] No tests added.** Plan asked for `ToolWindowInspectorPlatformTest`
   (4 scenarios) and `DialogInspectorPlatformTest` (4 scenarios) — both absent
   from `src/test/kotlin/.../core/platform/`. Other Phase-3 features in the same
   branch (`ListenerInspectorPlatformTest`, `ExtensionPointInspectorPlatformTest`,
   `ScreenshotCapturePlatformTest` …) all have platform tests; the gap is
   visible.

8. **[LOW] `ToolWindowInspector.kt:18`-`object ToolWindowInspector`.**
   Single-instance `object` is fine but blocks the test seam — the plan's
   `BasePlatformTestCase` test wants to construct an inspector against a
   fixture project, while the singleton looks up the project off
   `ProjectManager`. Convert to a class with a constructor parameter or a
   `listToolWindows(project, …)` overload so the platform test doesn't have
   to rely on the global "first open project".

9. **[LOW] Both inspectors do not call `ApplicationManager.assertIsDispatchThread()`.**
   The toolset wraps the call in `onEdtBlocking { … }`, but the inspectors
   are reusable from elsewhere; an assertion would catch a future caller that
   forgets the EDT bounce. Cheap insurance.

10. **[LOW] `DialogInspector.kt:23` — `Window.getWindows()` is thread-safe per
    AWT, but `dlg.bounds`, `dlg.title`, `dlg.isShowing` are EDT-only on some
    L&Fs. The wrapping `onEdtBlocking` covers us today; just don't refactor it
    away.

11. **[LOW] `UiArgs.kt:5-7` — KDoc explains why no args classes exist (good)
    but the existing `GetUiTreeArgs` / `FindByNameArgs` data classes here are
    not actually wired to the new tools (they're legacy). Confusing — the
    comment should clarify these classes are tree/find-only, not a
    requirement for new tools.

## Inlined-vs-inspector reconciliation

There is no duplicate. The toolset methods at
`UiInspectorToolset.kt:297-299` and `:332-334` are one-line bodies that
delegate to `ToolWindowInspector.listToolWindows(…)` and
`DialogInspector.listDialogs(…)` inside an `onEdtBlocking { … }` wrapper. The
salvaged inspector files are the ONLY implementations of those entry points.
Recommendation: **keep both files**, fix Findings 1, 2, 5, 6 in the
inspectors, leave the toolset wrappers untouched. The "inlined vs separate
file" concern from the brief stemmed from the fixer commit message — its
"placeholder" KDoc is misleading; the toolset always delegated.

## Plan-vs-implementation gaps

| Plan asked for | Implementation | Action |
|---|---|---|
| `providedByPluginId` via EP cache | hard-coded `null` | Finding 1 |
| `isVisible`/`isActive`/`isSplit`/`isFloating`/`isShowing` fields | renamed/dropped | Finding 2 |
| `id` field on `DialogInfo` | renamed to `componentId` | Finding 2 |
| Non-null `Bounds` | nullable | Finding 2 |
| Focused-project resolution via `IdeFocusManager` | `openProjects.firstOrNull()` only | Finding 5 |
| Procedural-icon → `null` rule | always `toString()` | Finding 6 |
| Platform tests (4 + 4) | none | Finding 7 |

The `@McpDescription` strings themselves are verbatim-from-plan and high
quality — that part is excellent. The drift is in the response models the
descriptions claim to return.

## Research notes (URLs)

- [`DialogWrapper.findInstance` source](https://github.com/JetBrains/intellij-community/blob/master/platform/platform-api/src/com/intellij/openapi/ui/DialogWrapper.java)
  — confirms upward `getParent()` walk; passing the `JDialog` itself works only
  when it IS the `DialogWrapperDialog`.
- [IntelliJ ToolWindowManager](https://github.com/JetBrains/intellij-community/blob/master/platform/platform-api/src/com/intellij/openapi/wm/ToolWindowManager.kt)
  — `getToolWindow(id)` and `toolWindowIds` are EDT-recommended; using
  `onEdtBlocking` is correct.
- [IntelliJ Threading Model](https://plugins.jetbrains.com/docs/intellij/threading-model.html)
  — confirms reads "allowed from any thread" but mutation requires EDT;
  per-ToolWindow getters (`isVisible`, `isActive`, `icon`, `contentManager`)
  are pragmatically EDT-only.
- [Tool Windows doc](https://plugins.jetbrains.com/docs/intellij/tool-windows.html)
  — recommends `ToolWindowManager.invokeLater()` over `Application.invokeLater()`
  for follow-up state changes; not relevant here (we only read), but worth
  bearing in mind for `ui.invoke_action_on` adjacent work.

## Test coverage assessment

Zero tests for the new code. The plan called for two
`BasePlatformTestCase`-backed platform tests, totalling 8 scenarios. None
exist. Minimum recommended additions:

- `core/platform/ToolWindowInspectorPlatformTest`: (1) `Project` tool window
  appears with `anchor="LEFT"`; (2) `nameContains` case-insensitive;
  (3) `includeInvisible=false` excludes hidden; (4) `providedByPluginId ==
  "com.intellij"` for `Project` (once Finding 1 is fixed).
- `core/platform/DialogInspectorPlatformTest`: (1) empty list when no dialogs
  open; (2) `JDialog` with known title surfaces with correct `isModal`;
  (3) minimal `DialogWrapper` resolves `contentClass` to its FQN, not
  `JDialog`; (4) `JDialog()` with no title emits `title=null`.

Unit tests for the toolset wrapper itself are unnecessary — it's a one-line
pass-through.

## Cross-cutting suggestions

- EDT bouncing in the toolset is correct (`onEdtBlocking { … }`, default 10 s
  cap, `ModalityState.any()` via the helper). Hard-rules-compliant.
- KDoc on `ToolWindowInspector` and `DialogInfo` literally says "Placeholder
  shape" and "Placeholder enumerator … in-progress". Either ship it as the
  real thing (drop the KDoc) or finish it. Right now the comment will rot.
- `runCatching` is used liberally inside the inspector loops to dodge
  per-window failures. That's the right call (mirrors the `arch.*` pattern
  where bundled plugins can throw), but failures should populate the
  `warnings` list — currently they're swallowed silently. The plan's edge
  case #4 explicitly calls this out: "Wrap per-window collection in
  `runCatching { … }`; on failure emit a `warnings` entry with the id".
- The `Bounds` model file in `model/Bounds.kt` is shared with other tools —
  good. Make `DialogInfo.bounds` non-null to match the plan and other
  consumers; emit `Bounds(0,0,0,0)` if AWT throws (and add a warning).
- Consider adding `ApplicationManager.getApplication().assertIsDispatchThread()`
  at the top of both inspector methods. Cheap, catches future misuse.

## References

- `src/main/kotlin/com/github/xepozz/ide/introspector/tools/UiInspectorToolset.kt:265-334`
  — the two `@McpTool` methods (the wrappers).
- `src/main/kotlin/com/github/xepozz/ide/introspector/core/ToolWindowInspector.kt`
  — needs Findings 1, 5, 6, 8 addressed.
- `src/main/kotlin/com/github/xepozz/ide/introspector/core/DialogInspector.kt`
  — needs Finding 4 description fix + warnings-on-failure.
- `src/main/kotlin/com/github/xepozz/ide/introspector/model/ToolWindowInfo.kt`
  — Finding 2 (field names + nullability).
- `src/main/kotlin/com/github/xepozz/ide/introspector/model/DialogInfo.kt`
  — Finding 2 (field names + nullability + missing `isShowing`).
- `src/main/kotlin/com/github/xepozz/ide/introspector/util/EdtHelpers.kt:20-36`
  — `onEdtBlocking` is correctly used.
- `src/main/kotlin/com/github/xepozz/ide/introspector/core/ComponentRegistry.kt`
  — dialog id stability across calls works as-is (WeakHashMap keyed on
  component identity); same id returned on repeat calls for the same dialog.
- `src/main/kotlin/com/github/xepozz/ide/introspector/core/internal/ExtensionMetadata.kt`
  — the cache infrastructure the plan wants to use for `providedByPluginId`.
- `docs/plans/ui-semantic-listing.md` — the contract this PR claims to
  implement.
