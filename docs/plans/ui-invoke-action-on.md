# `ui.invoke_action_on` — OPT-IN, SECURITY-SENSITIVE

## Purpose & motivation

Triggers an IntelliJ `AnAction` against a synthetic `AnActionEvent` whose
`DataContext` is rooted at a previously-located Swing component (by
`componentId` from `ui.find_by_*` / `ui.get_tree`). Lets an MCP agent press a
button, hit a context-menu entry, or fire any registered action against a
SPECIFIC widget — not just whatever currently holds focus.

Vs. JetBrains' built-in MCP server: the old OSS `ExecuteActionByIdTool` resolves
the data context from the focused frame — imprecise for tree/list/editor
context menus where click position matters. Our version targets a precise
component so the action sees the same data context a real user click would.

This is a **privileged write operation** — actions can delete files, run code,
mutate VCS state. It mirrors the `exec.*` security model exactly: off by
default, per-call modal confirmation, audited, double-prompt on a hard
blocklist, hard 10 s execution timeout.

**Success criterion:** an agent can press "Build Project" or invoke "Quick
Documentation" on a particular tree row in one tool call, without falling
back to free-form Kotlin via `exec.*`.

## Tool specification

### `ui.invoke_action_on`

**Signature:**
```kotlin
@McpTool(name = "ui.invoke_action_on")
@McpDescription("…verbatim block below…")
suspend fun ui_invoke_action_on(
    @McpDescription("…") actionId: String,
    @McpDescription("…") componentId: String,
    @McpDescription("…") presentationText: String? = null,
    @McpDescription("…") requireConfirmation: Boolean = true,
): InvokeActionResponse
```

**`@McpDescription` (verbatim, trim-margin):**
```
|Invokes an IntelliJ AnAction with a synthetic AnActionEvent whose DataContext is
|rooted at a previously-located Swing component. Equivalent to a real user clicking
|that widget and triggering the named action — but addressable from an MCP agent.
|
|OPT-IN and SECURITY-SENSITIVE. This is a privileged WRITE operation. Actions can
|delete files, run code, push to git, install plugins, refactor, etc. This tool is
|off by default and, when enabled, shows a modal confirmation dialog on every call
|(opt-out only for the rest of the session, never persisted).
|
|Use this when: you've already located the right Swing component via ui.find_by_*
|or ui.get_tree and you need to invoke an action whose data context depends on that
|component (a context-menu action on a tree node, a toolbar button bound to a
|specific panel, an editor action on a specific editor instance).
|
|Do NOT use this when:
|  - You want to invoke an action against whatever currently holds focus — use the
|    JetBrains built-in `execute_action_by_id` MCP tool, it's lighter-weight.
|  - You only need to read UI state (use ui.get_tree / ui.get_properties).
|  - The action you want is destructive (delete, reset, force-push, hard refactor)
|    AND you don't actually need the data context binding — issue the operation
|    via the dedicated MCP tool (VCS / refactor MCPs) which carries its own
|    confirmation UX instead of trusting our generic blocklist.
|  - The user hasn't enabled this tool: it's off by default in
|    Settings → Tools → IDE Introspector → "Allow UI action invocation".
|
|SAFETY MODEL (identical to exec.execute_kotlin_in_ide):
|  1. Off by default. Enable in Settings → Tools → IDE Introspector → "Allow UI
|     action invocation".
|  2. Per-call modal confirmation by default, showing actionId, action text, owning
|     plugin, target component class+id+bounds, and the current project. Opt-out
|     button "Allow for this session" — in-memory only, never written to disk.
|  3. HARD BLOCKLIST of dangerous action-id patterns (*Force*, *Delete*, *Reset*,
|     `Vcs.RefactoringChanges`, `Reset_HEAD`, `Maven.Reimport`, plus user-extendable
|     list in settings) triggers a SECOND confirmation dialog even when the session
|     bypass is active and even when requireConfirmation=false. There is no way to
|     bypass the second confirmation for blocklisted actions.
|  4. Every call recorded to idea.log under category "ide-introspector-audit"
|     (caller, actionId, componentId, component class, outcome, durationMs).
|  5. Hard 10 s execution timeout via onEdtBlocking(10_000). The IDE is not allowed
|     to hang for longer; the action either completes or the call returns
|     ok=false, error="timeout". Note: the action's side effects may continue to
|     run on the EDT after our timeout — this tool measures and reports, it does
|     not actually abort an in-flight action.
|
|Returns: { ok:bool, actionId:string, executed:bool, presentationText:string?,
|durationMs:long, error:string? }. `executed=true` ONLY when the action's
|actionPerformed() was actually invoked; `executed=false` means update() reported
|enabled=false (e.g. wrong context, dumb mode, no project) and we did not fire.
|`ok=false` reflects either user rejection, blocklist double-prompt rejection,
|unknown actionId, dead componentId, timeout, or thrown exception during update/
|perform.
|
|Examples:
|  actionId="Build", componentId="c_a3f2e1b8"
|    — invokes Build against a toolbar button context
|  actionId="QuickJavaDoc", componentId="c_91cd2204"
|    — context action on a tree row in the Project view
|  actionId="EditorChooseLookupItem", componentId="c_55ee0011"
|    — completion popup action targeted at one specific editor
```

**Args:**

| Name | Type | Default | Validation / notes |
|------|------|---------|--------------------|
| `actionId` | `String` | (required) | Non-blank. Looked up via `ActionManager.getAction(id)`; null → `ok=false, error="action-not-found"`. |
| `componentId` | `String` | (required) | Must resolve via `ComponentRegistry.lookup(id)`; null → `ok=false, error="component-detached"`. Ids do not survive IDE restart. |
| `presentationText` | `String?` | `null` | Optional override for `Presentation.text` on the synthetic event — purely cosmetic, used only in audit log and dialog. Truncated at 200 chars. |
| `requireConfirmation` | `Boolean` | `true` | Forces a confirmation prompt for this call even when the session bypass is active. Blocklisted ids ALWAYS double-confirm regardless of this flag. |

**Response model** (`model/InvokeActionResponse.kt`, new):
```kotlin
@Serializable data class InvokeActionResponse(
    val ok: Boolean,
    val actionId: String,
    val executed: Boolean,
    val presentationText: String? = null,
    val durationMs: Long,
    val error: String? = null,
)
```

## IntelliJ APIs used

- `ActionManager.getInstance().getAction(id)` — app-level service, thread-safe;
  may trigger class load on first call per id.
- `DataManager.getInstance().getDataContext(component)` — must run on EDT;
  produces a `DataContext` rooted at the component.
- `AnActionEvent.createFromAnAction(action, null, ActionPlaces.UNKNOWN,
  dataContext)` — preferred over manual constructor; populates modality and
  transferable context.
- `ActionUtil.lastUpdateAndCheckDumb(action, event, true)` — runs `update()`,
  respects `DumbAware`, returns false if disabled.
- `ActionUtil.performActionDumbAwareWithCallbacks(action, event)` — fires
  `AnActionListener` callbacks (other plugins see it as a real user click —
  important for audit consistency).
- `ModalityState.any()` for the EDT bounce (consistent with `ExecToolset`).
- `Logger.getInstance("ide-introspector-audit")` — reuse existing category.

Stability: `ActionUtil` is `@ApiStatus.Internal` in some versions but it's
the canonical invocation path, stable across 2023.x–2025.x.
`AnActionEvent.createFromAnAction(...)` is public stable API.

## Threading & EDT model

- MCP tool methods run on a background ktor coroutine (CLAUDE.md "Threading").
- `ActionManager.getAction` and our blocklist check happen off-EDT.
- The blocklist double-prompt confirmation runs via
  `UiActionConfirmationManager.confirm(...)` which itself uses
  `ApplicationManager.invokeAndWait { … }` (same pattern as
  `ConfirmationManager`).
- The full `getDataContext + update + performActionDumbAwareWithCallbacks` block
  runs inside ONE `onEdtBlocking(10_000) { … }` with `ModalityState.any()` —
  one EDT trip, not three. Anything more chops the action up across EDT events
  and breaks data-context lifetime (DataManager contexts are tied to the EDT
  pump that produced them).

## Timeout strategy

- Hard 10 s cap (CLAUDE.md). `onEdtBlocking` already defaults to
  `DEFAULT_EDT_TIMEOUT_MS = 10_000L`; we pass the same constant explicitly to
  make the intent grep-able.
- If the action fires but its work outlives 10 s (e.g. a build kicks off async
  work on a background thread), our coroutine returns `ok=true, executed=true,
  durationMs≈10000`. We measure the EDT round-trip, not the action's full
  effect — actions schedule their own progress and we don't follow them.
- If the EDT itself never returns within 10 s (rare: another modal mid-flight),
  return `ok=false, executed=false, error="edt-timeout"`. The action call has
  not happened.

## Edge cases

1. **Unknown `actionId`** — `getAction` returns null. Skip confirmation, return
   `ok=false, executed=false, error="action-not-found:$actionId"`. Audited.
2. **Dead `componentId`** — `ComponentRegistry.lookup` returns null. Same
   `McpExpectedError` `ui.get_properties` uses (line 232–236 of `UiInspectorToolset`).
3. **Settings disabled** — throw `McpExpectedError` BEFORE confirmation or audit.
4. **`update()` reports enabled=false** — do NOT call `actionPerformed`. Return
   `ok=true, executed=false, presentationText=event.presentation.text` so the
   agent sees why it grayed out.
5. **Dumb mode** — handled by `lastUpdateAndCheckDumb`; non-`DumbAware` actions
   short-circuit to enabled=false.
6. **Blocklisted action with session bypass active** — second confirmation
   ALWAYS fires; session bypass affects only the first prompt.
7. **Action throws** — `runCatching`. Return `ok=false, executed=true,
   error=throwable.message`. IDE stays up.
8. **Component detached mid-call** — between `lookup` and EDT block the panel
   could close. Inside `onEdtBlocking` re-check `component.isShowing`; if
   false, `error="component-not-showing"`.
9. **Editor-context actions** — `DataManager.getDataContext` on the editor
   component supplies `CommonDataKeys.EDITOR`; on a wrapping panel it may not.
   If `update()` disables, that's the correct outcome — agent re-targets.
10. **`presentationText` injection** — applied AFTER `update()` runs, else the
    action's own `update()` overwrites it. Affects audit/dialog only.

## Files to create/modify

| Path | Op | What |
|------|----|------|
| `src/main/kotlin/com/github/xepozz/ide/introspector/tools/UiInspectorToolset.kt` | Edit | Add `@McpTool fun ui_invoke_action_on(...)`. NOTE in a header comment that this is the ONE non-pure-read method in this toolset; all others are introspection. Mirrors the opt-in pattern of `ExecToolset`. |
| `src/main/kotlin/com/github/xepozz/ide/introspector/core/UiActionInvoker.kt` | Create | Headless logic: resolve action+component, build synthetic event, `lastUpdateAndCheckDumb`, `performActionDumbAwareWithCallbacks`, format error, measure durationMs. Returns a plain `InvokeActionResult` (not the `@Serializable` response — the toolset wraps). |
| `src/main/kotlin/com/github/xepozz/ide/introspector/exec/UiActionSettings.kt` | Create | New `@State` `PersistentStateComponent`. Storage file SHARED with `ExecSettings` (`ide-introspector.xml`), distinct `@State.name`. Fields: `enabled=false`, `confirmationRequired=true`, `auditEnabled=true`, `blocklistedActionIds: List<String> = DEFAULT_BLOCKLIST`. |
| `src/main/kotlin/com/github/xepozz/ide/introspector/exec/UiActionConfirmationManager.kt` | Create | Sibling of `ConfirmationManager`. Two-stage: normal prompt (with session bypass), then a forced second prompt if the actionId hits the blocklist (NO session bypass for stage 2). Renders actionId, action text, plugin owner, target component class/id/bounds, project name. |
| `src/main/kotlin/com/github/xepozz/ide/introspector/exec/UiActionBlocklist.kt` | Create | `object` with `DEFAULT_PATTERNS = listOf("*Force*","*Delete*","*Reset*")` and `DEFAULT_IDS = listOf("Vcs.RefactoringChanges","Reset_HEAD","Maven.Reimport", …)`. Single `matches(actionId): Boolean` that runs both against the union of (defaults ∪ user-extended list from settings). |
| `src/main/kotlin/com/github/xepozz/ide/introspector/exec/AuditLogger.kt` | Edit | Add `recordUiAction(actionId, componentId, componentClass, outcome, durationMs, error?)` overload using the SAME `Logger("ide-introspector-audit")`, distinct prefix `[ui.invoke_action_on]`. Keep existing `record(...)` for exec calls untouched. |
| `src/main/kotlin/com/github/xepozz/ide/introspector/exec/ExecSettingsConfigurable.kt` | Edit | Add a second labelled section "Tier 2: UI action invocation" with three checkboxes (enabled / confirmationRequired / auditEnabled) bound to `UiActionSettings`. Reuse the existing Configurable rather than introduce a second settings entry — same security category. |
| `src/main/kotlin/com/github/xepozz/ide/introspector/model/InvokeActionResponse.kt` | Create | `@Serializable InvokeActionResponse` data class (shape under "Response model" above). |
| `src/main/kotlin/com/github/xepozz/ide/introspector/model/args/UiActionArgs.kt` | Create (optional) | Only if we standardise on arg classes; otherwise inline. Current `UiInspectorToolset` uses inline args — keep consistent and skip this file. |
| `src/main/resources/META-INF/plugin.xml` | Edit | Add `<applicationService serviceImplementation="com.github.xepozz.ide.introspector.exec.UiActionSettings"/>` next to the existing `ExecSettings` registration. NO additional `applicationConfigurable` — we extend the existing one. |
| `src/test/kotlin/com/github/xepozz/ide/introspector/core/UiActionInvokerTest.kt` | Create | Unit: error-message formatting, presentationText truncation, blocklist matcher. |
| `src/test/kotlin/com/github/xepozz/ide/introspector/core/platform/UiActionInvokerPlatformTest.kt` | Create | `BasePlatformTestCase` — see test plan. Sets `UiActionSettings.enabled = false` in `tearDown` so no test residue. |

**Loading shim choice (open question, see below):** No new META-INF shim. The
new tool sits inside `UiInspectorToolset` which is already registered in
`mcp-integration.xml` (always-on when `com.intellij.mcpServer` is present). The
`UiActionSettings.enabled=false` default is what keeps it inert until the user
flips it. Adding a new shim file would imply the tool isn't loaded at all when
disabled — but the existing pattern is "load the tool, refuse the call".
Consistent with `exec.*`.

## Confirmation flow design

Introduce a sibling `UiActionConfirmationManager` rather than extending
`ConfirmationManager`:

- Different layouts: exec shows a code text area; action shows a structured
  label list (id, text, plugin, target component, project).
- Session-bypass state MUST be separate — bypassing exec confirmations should
  not bypass action confirmations (different threat models: typed code vs.
  third-party plugin action behind a stable id).
- Two-stage blocklist prompt is action-specific; layering it into the exec
  dialog would muddy that code path.

Both managers stay ~60 LOC. A shared `SecurityConfirmation` base is a
candidate refactor only if a third opt-in tool appears.

## Test plan

**Unit (`UiActionInvokerTest`):**
- `UiActionBlocklist.matches("ForceDeleteFile") == true`,
  `matches("RunAction") == false`, `matches("Reset_HEAD") == true`,
  `matches("MyForceUpdater") == true` (wildcard).
- `presentationText` longer than 200 chars truncates to 200 + ellipsis.
- Error-message formatter renders `action-not-found:<id>` and
  `component-detached:<id>` consistently.

**Platform (`UiActionInvokerPlatformTest extends BasePlatformTestCase`):**
- `setUp()` flips `UiActionSettings.enabled=true` and
  `confirmationRequired=false` so CI never blocks on a modal; `tearDown()`
  restores defaults.
- `EditorEscape` against the active editor component → `ok=true, executed=true`.
- `actionId="DoesNotExist_$ts$"` → `ok=false, executed=false,
  error startsWith "action-not-found"`; no confirmation dialog.
- Real action with `componentId="c_deadbeef"` → `McpExpectedError` (parity
  with `ui.get_properties`).
- Blocklisted id (`Vcs.RefactoringChanges`) with `confirmationRequired=false`
  STILL prompts (simulated via TestDialog override); rejection yields
  `ok=false, error="user-rejected-blocklist"`.
- Audit log captures one INFO line per invocation under category
  `ide-introspector-audit`; verified via `TestLoggerFactory`.

**Smoke (manual, runIde):** flip the setting on, invoke `About` against the
main frame — About dialog appears; tool returns `ok=true, executed=true`.

## Estimated effort

~1.5 days:
- 0.25 d — `InvokeActionResponse` model + `@McpDescription` text + plugin.xml
  wiring + settings field added to existing Configurable.
- 0.5 d — `UiActionSettings` + `UiActionBlocklist` + `UiActionConfirmationManager`
  (two-stage prompt, separate session bypass, structured layout).
- 0.25 d — `UiActionInvoker` core (resolve, synth event, update-check,
  perform, error mapping, timing).
- 0.25 d — `UiInspectorToolset.ui_invoke_action_on` wrapper + `AuditLogger`
  overload.
- 0.25 d — unit + platform tests + manual smoke under `./gradlew runIde`.

## Open questions / risks

1. **Loading shim placement.** Plan keeps the tool inside the always-on
   `UiInspectorToolset`. Alternative: split into its own `UiActionToolset`
   registered in a new `ui-action.xml` shim. Pro: clearer opt-in signal in
   plugin.xml. Con: dynamic toolset loading based on settings isn't how
   the IntelliJ plugin model works — registrations are static.
   **Recommendation:** keep inside `UiInspectorToolset`, refuse the call
   when disabled, matching `exec.*`.
2. **User-editable blocklist.** `List<String>` serialises out of the box;
   a `JBList` editor in the Configurable adds ~0.25 d. **Recommendation:**
   ship with defaults only in v1; defaults remain editable via XML on
   disk for power users. Add Configurable editor in a follow-up.
3. **Audit log format.** Exec writes one human-readable line; richer JSON
   would help analysis but breaks the existing convention.
   **Recommendation:** keep one line per call, same format as exec, with
   `[ui.invoke_action_on]` prefix so `grep` distinguishes them.
4. **Action class loading cost.** `getAction(id)` triggers class load — a
   pathological constructor could exceed the 10 s cap. Mitigation: wrap
   `getAction` in `withTimeoutOrNull(2_000)`, return `error="action-load-failed"`
   on timeout. Separate from the 10 s EDT cap on perform.
5. **`AnActionListener` fan-out.** `performActionDumbAwareWithCallbacks`
   fires every registered `AnActionListener` (telemetry in 3rd-party
   plugins). Correct for "look like a real click"; consent model is
   "you allowed UI actions", not "silent ones". Keep.
6. **Overlap with JetBrains' built-in.** If the bundled MCP server adds
   `invoke_action_on_component`, our differentiator is explicit componentId
   targeting and blocklist double-prompt. Worth keeping either way.

## References

- Opt-in template: `ExecToolset` at
  `/home/user/ide-introspector-plugin/src/main/kotlin/com/github/xepozz/ide/introspector/tools/ExecToolset.kt`.
- `ComponentRegistry.lookup` pattern: `UiInspectorToolset.ui_get_properties`
  lines 230–240.
- Settings shape: `exec/ExecSettings.kt`. Dialog skeleton:
  `exec/ConfirmationManager.kt`. Audit: `exec/AuditLogger.kt`.
- IntelliJ source — `ActionUtil`:
  `https://github.com/JetBrains/intellij-community/blob/master/platform/platform-impl/src/com/intellij/openapi/actionSystem/ex/ActionUtil.java`.
- JetBrains OSS MCP — `ExecuteActionByIdTool` in
  `https://github.com/JetBrains/mcp-server-plugin` resolves data context from
  the focused frame; we resolve from a caller-specified component.
