# De-reflection plan — replace fragile reflection with compiler-checked APIs

Status: ACTUALIZED after the 2026.0.x de-duplication pass. Motivation: MCP requests fail on
some users' projects on the 2026.0.3 build. The actual cause turned out to be a volatile
mcpServer binding (see ACTUAL ROOT CAUSE), not internal-API reflection — and that finding
also re-shapes the remaining de-reflection work (see Re-assessment).

## Verified facts (checked against source, not guessed)

- `PluginManagerCore.plugins` on current platform is `@JvmStatic @get:ApiStatus.Internal
  val plugins: Array<IdeaPluginDescriptor>` — the Java getter `getPlugins()` returns an
  **array**. So `PluginLookup.allPlugins()`'s cast to `Array<IdeaPluginDescriptor>` is
  correct on the target platform; the plugin-enumeration cast is **NOT** the failure cause.
  (Source: `platform/core-impl/src/com/intellij/ide/plugins/PluginManagerCore.kt`.)
- Pinpointing the actual failing request still needs the stacktrace / failing tool name.

## ACTUAL ROOT CAUSE of the 2026.0.3 failures (from runtime log — supersedes the reflection hypothesis)

Analysis of `IU-2025.2/log/idea.log` shows the failing requests are NOT our tool logic and
NOT reflection. Two independent issues:

1. **SSE transport drops the session (framework / mcpServer plugin, not our code).**
   The MCP-over-SSE stream closes on idle/timeout; each `/mcp` opens a new `sessionId`, the
   server discards the old one, but the Claude client keeps POSTing tool calls to the stale
   `sessionId` → server logs `WARN Session not found for sessionId: …` → HTTP 404. Long calls
   (e.g. `execute_run_configuration`, 90–120 s) and idle gaps make the SSE stream drop, so the
   next call 404s. Our own tools cap at 10 s (good) and do not hold the stream; this is the
   `com.intellij.mcpServer` transport, outside our plugin. Mitigation on our side: none beyond
   the existing 10 s cap. Track upstream / document as a known limitation.

2. **`ClassNotFoundException: com.intellij.mcpserver.McpCallInfoKt` (OUR fix).**
   `McpCallInfoKt` is the mcpServer class that almost certainly declares the
   `currentCoroutineContext().projectOrNull` extension. We compile against
   `com.intellij.mcpServer:252.28238.29`, but IDE builds ship different mcpServer versions
   where that class is renamed/moved/absent. So every tool that resolves the project via
   `requireProject()` → `projectOrNull` fails to link on those builds:
   `IdeStateToolset` (`ide.indexing_status`), `EditorToolset` (`editor.*`), `PsiToolset`
   (`psi.*`). Tools that don't touch `projectOrNull` (`ui.*`, `arch.*`, `screenshot.*`) keep
   working — which matches the observed partial failure.

### Fix for issue 2 (decouple project resolution from the volatile mcpServer API) — DONE

Implemented: `util/IdeProjectResolver.focusedProject()` (ProjectManager + IdeFocusManager,
pure platform API); `PsiToolset`/`IdeStateToolset`/`EditorToolset` now resolve the project
through it instead of `projectOrNull`. Verified at the bytecode level: those three classes
reference `IdeProjectResolver` and have **0** references to `McpCallInfoKt`. Confirmed via the
bundled jar that `projectOrNull` is declared in `com.intellij.mcpserver.McpCallInfoKt`.

Original notes:
- Replace `currentCoroutineContext().projectOrNull` with a stable **platform** resolver, e.g.
  `ProjectManager.getInstance().openProjects` → single open project, else the most-recently
  focused (`WindowManagerEx`/`IdeFocusManager`), else first. This removes the `McpCallInfoKt`
  bytecode reference entirely. Pure, compiler-stable platform API.
- Centralise in ONE `requireProject()` helper shared by `PsiToolset` / `IdeStateToolset` /
  `EditorToolset` (currently duplicated) so the binding lives in one place and is unit-coverable
  via a thin seam.
- Optionally accept the framework-provided `projectPath` arg and match it against
  `Project.basePath` for multi-project precision, falling back to the platform resolver.
- Verify the link first: decompile the bundled `mcpServer` jar (252.28238.29) to confirm
  `projectOrNull` is declared in `McpCallInfoKt`; and check which mcpServer version the failing
  IDE actually bundles (compatibility range mismatch).

Note: the de-reflection work below is still worthwhile hardening, but it is NOT the cause of the
current failures.

## Current state (after the de-duplication pass — commits 18b4ffa, 922c9b7, 6cb0025)

The dedup pass **centralized** reflection; it did **not** eliminate it. Concretely:
- All `readField`/`readMethod`/`readEnumName` go through one tested `util.ReflectionAccess`
  (vararg candidate-names, superclass-hierarchy walk, `setAccessible`, WARN-on-real-error).
- Descriptor/container reflection extracted to `core/internal/ContainerDescriptorReader`;
  plugin id/name reflection to `core/internal/PluginDescriptorReader`. Per-adapter mapping
  split into pure `adapterToExtensionInfo` (partial-results-on-throw restored).
- The reflection is now **DRY, logged, and test-adjacent — but still reflection.** The
  original tier-1 goal (replace internal-API reflection with DIRECT TYPED CALLS so the
  compiler gates drift) was **never started**.
- `McpCallInfoKt`/`projectOrNull` decoupling is DONE for every project-resolving tool,
  including `CodeSourceToolset` and `ExecToolset` (both moved to `IdeProjectResolver`).

## Re-assessment (the McpCallInfoKt lesson changes the plan)

The original thesis was "go direct, let the compiler gate drift." But the 2026.0.x failure
was *exactly* a direct binding to a member (`projectOrNull`) that was **absent on the user's
IDE build** → hard `ClassNotFoundException` at runtime. We support an **open version range**
(`sinceBuild 252`, no `untilBuild`). Converting `@ApiStatus.Internal` members to direct typed
calls would reintroduce the SAME failure class (`NoSuchMethodError`/`NoSuchFieldError`) on any
build where the internal API differs — moving the risk from one symbol to dozens.

**Conclusion:** for genuinely `@ApiStatus.Internal` members, the centralized
reflection-with-graceful-fallback we now have is the *safer* end-state — NOT direct calls.
Direct typed calls are worth it **only where the member is genuinely public/stable API.**

## Remaining work (re-scoped)

### A. Worth doing — direct typed calls for PUBLIC members only (compiler-checked, zero version risk)
- `core/ComponentSerializer.kt` — `ActionButton.getAction()`: `com.intellij.openapi.actionSystem.impl.ActionButton`
  is public; call it directly instead of reflecting by method name.
- `core/internal/PluginDescriptorReader.kt` — id/name come from `IdeaPluginDescriptor.getPluginId().idString`
  and `.getName()`, both PUBLIC. The `pd: Any?` reflection can be mostly direct; keep a thin
  fallback only for the light-service path where the descriptor type isn't statically known.

### B. Keep as centralized reflection — do NOT convert (internal API on an open range)
- `core/PluginLookup` (PluginManagerCore, `@ApiStatus.Internal`), `core/ExtensionPointInspector`
  (ExtensionPointImpl/adapters), `core/internal/ContainerDescriptorReader` (ContainerDescriptor),
  `ServiceInspector`/`ListenerInspector` descriptor fields. Already DRY + logged + tested via
  `ReflectionAccess`/the readers — that is the correct resilience posture here.

### C. Keep isolated (tier-3 — correct as-is)
- `exec/KotlinExecutor` cross-classloader compiler bridge (minor nit: replace `.single { }`
  with `.firstOrNull()` + explicit error), `JavaPsiFacade` probes (`FqnLink`/`MembersSection`/
  `JavaMembersPreview`/`DetailForm` `javaPsiAvailable` gate), `UiInspectorContextProvider`
  (`UiInspectorToolset`), `TopicInspector` `Class.forName(initialize=false)`,
  `ExtensionMetadata` bean-field harvest.

### D. Still open (cross-cutting)
- **Propagate real reflection errors into the tool response** (today: WARN-logged + null
  fallback). Deferred — would change the response contract; do it as a deliberate opt-in
  diagnostic field, not a silent change.
- **Optional health-check probe** — a tool/service that exercises each reflective access once
  and reports which are broken on the running IDE build, turning silent degradation into a
  visible signal.

## Execution status
1. DONE — `McpCallInfoKt`/`projectOrNull` fix across all project-resolving tools.
2. DONE — Consolidate reflection into `ReflectionAccess` (+ vararg / `readEnumName` / WARN
   logging) and `ContainerDescriptorReader` / `PluginDescriptorReader`.
3. RE-SCOPED — tier-1 direct calls: do **only** for public members (A above); do NOT convert
   internal-API sites (B) — that reintroduces the McpCallInfoKt failure class.
4. DROPPED — blanket direct-call conversion of the internal inspectors (see Re-assessment).
5. DROPPED — `IdePlatformCompat` tier-2 layer: unnecessary while we keep centralized reflection.
6. OPEN — items in D, then `./gradlew build` + live re-verify via MCP.
