# De-reflection plan — replace fragile reflection with compiler-checked APIs

Status: planning. Motivation: MCP requests fail on some users' projects on the
2026.0.3 build; reflection on internal IntelliJ APIs (which drift across IDE builds and
silently swallow errors) is the prime structural suspect.

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

## Core thesis

The internal impl classes we reflect on (`ExtensionPointImpl`, `ExtensionComponentAdapter`,
`ContainerDescriptor`, `ServiceDescriptor`, `ListenerDescriptor`, `PluginManagerCore`) are on
the **compile classpath** when building against the IntelliJ Platform. `@ApiStatus.Internal`
is a plugin-verifier concern (CI-only, already accepted here), NOT a compile barrier.

Therefore: **call them directly with real types.** Benefits:
- The compiler validates every member on each platform bump — drift fails `compileKotlin`
  / CI instead of failing at the user's runtime.
- Removes `ClassCastException` / `NoSuchMethodError` risk that reflection hid.
- Removes silent degradation: today the `catch (_: Throwable) -> null` helpers turn a real
  incompatibility into empty results that look like success.

Trade-offs (acceptable): verifier warnings (CI-only, see CLAUDE.md "Known plugin-verifier
warnings"); a harder binding to the supported platform range (sinceBuild 252).

## Strategy — 3 tiers

1. **Direct typed call** — for any symbol on the 252 compile classpath. This is the default
   and covers most HIGH/MEDIUM sites. Reflection there was only verifier-avoidance/habit.
2. **Typed call + thin reflection fallback** — only for members genuinely renamed within the
   supported range (e.g. `getSortedAdapters`/`sortedAdapters`, `className`/`myClassName`).
   Primary path is the direct typed call; the fallback lives in ONE `IdePlatformCompat`
   object with a unit test asserting which path is chosen.
3. **Keep isolated reflection** — only where the symbol is NOT always on the classpath:
   - cross-classloader compiler bridge in `KotlinExecutor` (embedded Kotlin compiler);
   - optional modules absent in some IDEs (`JavaPsiFacade` in non-Java IDEs —
     `FqnLink`, `MembersSection` already do this correctly; leave them);
   - `UiInspectorContextProvider` (internal, optional) — already cleanly guarded.

## Compiler-testability

- Direct calls = type-checked against the target SDK; renamed/retyped members break the
  build, never the user.
- Extract the pure adapter→model mapping into pure functions with JUnit tests (pattern:
  `EditorTabsAssembler`). The reflection today mixes extraction with mapping; split them.
- Residual reflection sits behind one `IdePlatformCompat` boundary with a contract unit test.

## Inventory by risk (file:line — action)

### HIGH — internal API, version-fragile
- `core/PluginLookup.kt:21,25,29` — `Class.forName("PluginManagerCore")` + `getMethod`
  `getPlugins`/`getPlugin`. Action: call `PluginManagerCore.getPlugins()` /
  `getPlugin(id)` directly (typed). Accept verifier warning. Keep `PluginLookup` as the
  single typed façade.
- `core/ExtensionPointInspector.kt:56,69,104,119,138,159,170,186,220,229` — `getExtensionPoints`,
  `extensionPoints` field, EP `name`/`getName`, `getKind`, `className`/`myClassName`,
  `getExtensionClass`, `getPluginDescriptor`, `isDynamic`, `getSortedAdapters`/`sortedAdapters`,
  adapter `implementationClassOrName`/`pluginDescriptor`. Action: tier-1 direct calls on
  `ExtensionsAreaImpl`/`ExtensionPointImpl`/`ExtensionComponentAdapter`; tier-2 compat only
  for the genuinely variant names (`getSortedAdapters`/`sortedAdapters`, `className`/
  `myClassName`). Move the model mapping to a pure tested function.

### MEDIUM — internal API, reasonable fallbacks today
- `core/ListenerInspector.kt:55,73,81,87` — `getAppContainerDescriptor`/`getProjectContainerDescriptor`,
  `ContainerDescriptor.listeners`, `ListenerDescriptor.*`. Action: direct typed access.
- `core/ServiceInspector.kt:76,80,87,131,170,179,184,202` — `ServiceDescriptor.preload/os/
  configurationSchemaKey`, `ContainerDescriptor.services`, `processAllImplementationClasses`.
  Action: direct typed access; keep `@Service` annotation reads (already public/JDK).
- `core/internal/ExtensionMetadata.kt:32` — generic bean-field harvest. Action: keep
  (genuinely generic), but log failed reads instead of swallowing.
- `core/ComponentSerializer.kt:97` — `ActionButton.getAction()`. Action: `ActionButton` is
  public UI API; call directly (typed) and drop the reflection.
- `exec/KotlinExecutor.kt:104,173,256` — generated `Plugin.run`, embedded-compiler `compile`,
  `JavaSdkUtil.getJdkClassesRoots`. Action: keep (cross-classloader / generated code), but
  replace `.single { }` with `.firstOrNull()` + explicit error; surface real errors.

### LOW — keep as-is (correct/idiomatic)
- `tools/UiInspectorToolset.kt:637` `UiInspectorContextProvider` — optional, well guarded.
- `toolwindow/details/FqnLink.kt`, `MembersSection.kt`, `JavaMembersPreview.kt` — optional
  Java module probe; correct.
- `core/TopicInspector.kt` — `Class.forName(initialize=false)` + JDK generic-type reads;
  intentional and safe.
- `core/PsiUsageSearcher.kt:181` `simpleName` heuristics; JDK `getClass()` calls.

## Cross-cutting fixes (also help diagnose the current failures)

- **Stop swallowing `Throwable`.** The `readField`/`readMethod` helpers return `null` on any
  exception, turning incompatibilities into silent empty results. Distinguish
  member-not-found from `LinkageError`/`NoSuchMethodError`, log at warn, and propagate real
  errors into the tool response so a failing request reports *why*.
- **De-duplicate** the three identical `readField`/`readMethod` helpers
  (`ExtensionPointInspector`, `ListenerInspector`, `ServiceInspector`) into one tested util,
  used only by the residual tier-2 fallback.

## Execution order

1. (Blocked on info) Get the failing tool name / stacktrace → fix that specific bug first.
2. Tier-1 for HIGH: `PluginLookup`, `ExtensionPointInspector` → direct typed calls, pure
   mapping + tests, stop swallowing errors. Build becomes the gate.
3. Tier-1 for MEDIUM inspectors: `ServiceInspector`, `ListenerInspector`, `ComponentSerializer`.
4. Introduce `IdePlatformCompat` for the few tier-2 variant members + unit test.
5. Consolidate `readField`/`readMethod`; add logging.
6. `./gradlew build` (compiler now guards) + live re-verify via MCP.
