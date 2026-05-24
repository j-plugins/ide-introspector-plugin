# Review: arch.list_services

Branch: `claude/project-features-analysis-odEwP` @ HEAD (`3c15123`)
Scope:
- `core/ServiceInventory.kt`
- `model/ServiceInfo.kt`
- `model/args/ArchArgs.kt` (new `ListServicesArgs`)
- `tools/ArchitectureToolset.kt` (new `arch_list_services` `@McpTool`)
- `core/PluginInventory.kt` (Snapshot extension)
- `src/test/kotlin/.../core/ServiceInventoryReflectionTest.kt`
- `src/test/kotlin/.../core/platform/ServiceInventoryPlatformTest.kt`

Plan: `docs/plans/arch-list-services.md`.

## Verdict

**Ship-ready with one MEDIUM gap.** The shape is right — descriptor POJOs
are read reflectively without ever instantiating a service, the Snapshot
gains `services` / `servicesByPlugin`, the `@McpDescription` follows the
5-section template verbatim from the plan, both the `PreloadMode` enum
and legacy `Boolean` paths are exercised by the reflection tests, and
the platform test verifies application + project + per-plugin shapes
against the running sandbox. The single substantive gap is that
ServiceInventory does **not** mirror `ListenerInspector.kt:62-70`'s
legacy field-name fallback (`appContainerDescriptor` → `app`,
`projectContainerDescriptor` → `project`), so the tool silently returns
an empty list when run against a pre-252 platform — which is unlikely
in production but is exactly the situation the CLAUDE.md "containerDescriptor
field rename" note exists to guard against.

## Findings

### Correctness

1. **[MEDIUM] `core/ServiceInventory.kt:33-43` — missing legacy
   `app` / `project` container-field fallback.** `ListenerInspector`
   handles the same 251→252 rename with
   `containerFields = listOf("appContainerDescriptor", "app")` and
   the in-code comment "Earlier 251.x builds used `app` / `project` —
   try the canonical names first, fall through to the legacy aliases
   for safety." ServiceInventory only probes the modern
   `appContainerDescriptor` / `projectContainerDescriptor` /
   `moduleContainerDescriptor` names. Two consequences:
   - Build verifier reports against earlier `sinceBuild` targets will
     show an empty services list with no error, masking a real
     regression behind "the IDE genuinely has no services".
   - The CLAUDE.md "Common pitfalls" entry the reviewer was pointed
     at specifically calls out this rename — the sibling inspector
     paid the cost of pluralising the field-name list; this one
     should too. Trivial fix: add `"app"` / `"project"` /
     (no module equivalent — module containers are new in 252) as
     second-position fallbacks. While there, hoist the shared list to
     a constant on the object.

2. **[LOW] `ServiceInventory.kt:79-87` — `readServicesList` reads
   `services` via getter first, but ContainerDescriptor declares it
   `@JvmField val`.** The platform's `ContainerDescriptor.services` is
   `@JvmField val services: List<ServiceDescriptor>` (verified — see
   References). `@JvmField` bypasses Kotlin getter synthesis, so
   `readMethod(container, "getServices")` is always a miss; every call
   pays a `Class.getMethods()` scan that returns nothing. Not a
   correctness bug — the `readField` fallback works — but every
   descriptor pays the cost (≈ N plugins × 3 scopes × pointless
   getMethods scan). Flip the order: `readField("services")` first,
   `readMethod("getServices")` as the version-drift fallback. Mirror
   change for `readContainerDescriptor` is NOT applicable because
   `IdeaPluginDescriptorImpl`'s container properties really are
   auto-getter Kotlin properties (no `@JvmField`).

3. **[LOW] `ServiceInventory.kt:127-134` — `readBoolMember` getter
   name probe is mis-prioritised for the `overrides` field.**
   `ServiceDescriptor` is a `.java` class with `public final boolean
   overrides`. The probe sequence is `isOverrides` → `getOverrides`
   → field. Neither method exists, so every descriptor pays two
   pointless `getMethods()` scans before the field lookup succeeds.
   For the platform's ~1500 services this is in the "free at human
   scale" bucket but worth a one-line reorder for symmetry with
   finding 2: field first, getter as drift fallback. Equally fine to
   leave alone if you'd rather not touch reflective helpers.

4. **[LOW] `ServiceInventory.kt:79-86` — unchecked cast
   `viaGetter as List<Any?>` is unsafe if a future
   `getServices()` returns `Collection<ServiceDescriptor>` instead
   of `List`.** The `is List<*>` check passes for any `List<*>`,
   then `as List<Any?>` is technically an unchecked covariant cast.
   Idiomatic in this codebase (matches `ExtensionPointInspector`),
   so no change required — flagged only because the JVM-level type
   check is a noop after erasure and a future API drift to
   `Collection` would crash here rather than fall through to the
   field read.

5. **[INFO] `ServiceInventory.kt:18-25` — `listServices(scope)` re-
   filters by scope inside the inner loops AND the outer caller
   re-filters again at the toolset layer.** Acceptable — the toolset
   passes `services()` which is always pre-collected at `"all"` (see
   `PluginInventory.collect:83`), so the `scope` parameter to
   `ServiceInventory.listServices` is effectively dead at runtime;
   the parameter exists only for direct callers (the platform test
   does use it at line 117). Either drop the scope arg and document
   "use `.filter { it.scope == ... }` post-hoc", or keep it but note
   it's a convenience filter not a perf optimisation. Plan §"Threading
   & EDT" assumed enumeration cost is uniform so this is fine.

6. **[INFO] No `@Service` light-services coverage — but the plan
   acknowledges this and the `@McpDescription` explicitly warns
   callers.** The description string lines 533-534 read: `"@Service-
   annotated light services registered without plugin.xml are NOT
   enumerated here (see arch.find_extenders_of)"`. This is the
   correct stance — light services live outside
   `containerDescriptor.services` (confirmed via plugins.jetbrains.com
   docs — see References). Plan open question 4 is resolved correctly.

### Threading

7. **[OK] No EDT bouncing, no ReadAction.** `PluginManagerCore.plugins`
   and `PluginDescriptor` field reads are immutable / lock-free. The
   `arch.*` blanket rule ("don't need EDT bouncing") from CLAUDE.md
   applies cleanly. The `suspend fun` runs on the ktor coroutine,
   reads the cached `Snapshot`, applies filters in-loop, returns.
   No 10 s timeout risk — even an empty cache, full enumeration is
   <100 ms per the plan's accurate estimate.

### Caching

8. **[OK] Snapshot extension is correctly scoped.** `services` is
   collected eagerly inside `PluginInventory.collect()` under the same
   30 s TTL as plugins + EPs; both arch.* MCP calls and the Platform
   Explorer tool window will share one snapshot. `refresh()` invalidates
   everything in one shot. The `runCatching { ... }.getOrElse {
   emptyList() }` wrapper at `PluginInventory.kt:83` correctly mirrors
   `ExtensionPointInspector.collectFromArea` — one corrupt third-party
   plugin can't poison the snapshot.

### Test coverage

9. **[OK] Reflection tests cover the version-drift surface
   thoroughly.** 28 unit tests exercise: enum-vs-Boolean `preload`,
   getter-vs-field access on both interface/impl and container,
   blank-string and null fallbacks, the swallow-on-throw guarantee,
   and all five filter dimensions. The synthetic-POJO pattern matches
   `ListenerInspectorReflectionTest`. Good shape.

10. **[OK] Platform test asserts what matters without brittling on
    specific FQNs.** Test names: list is non-empty, application
    scope is present, project scope is present, `com.intellij*`
    provider is present, every entry has non-blank interface+impl,
    `servicesByPlugin` filters correctly, refresh produces a new
    list, direct `ServiceInventory` and cached `PluginInventory`
    agree on count, scope-filter is exclusive. Mirrors
    `PluginInventoryPlatformTest`. The plan's `testNoneInstantiated
    AfterEnumeration` is correctly omitted — would be flaky for the
    reasons the plan itself documented; `assumeTrue` skips defeat
    the purpose of having the assertion.

11. **[LOW] No platform test asserts a known
    `serviceImplementation` FQN.** Plan §"Test plan" suggested
    `com.intellij.openapi.fileEditor.FileEditorManager` as a
    candidate after one observed run. Not strictly necessary — the
    "platform provider exists" check is already a regression net —
    but would catch a future drift where the descriptor's field name
    changes and our reflection silently returns
    `serviceImplementation=""`. Strengthen via something like:
    `assertTrue(services.any { it.serviceInterface ==
    "com.intellij.openapi.fileEditor.FileEditorManager" })`.

12. **[LOW] No test for `overrides=true` services.** Plan edge case
    9 calls out `overrides=true` declarations exist in some platform
    plugins, and the reflection test asserts `overrides` is read,
    but neither the unit nor the platform test asserts that a real
    `overrides=true` entry surfaces. If it ever stops bubbling
    up, only an inspecting human would notice. Optional.

### Plan-vs-impl gaps

13. **`ListServicesArgs` is declared but never consumed.** Plan
    §"Files to create/modify" lists it as expected output; the
    `arch_list_services` method instead uses individually-annotated
    parameters (matching every sibling tool). Confirmed consistent
    with `ListExtensionPointsArgs`, `ListPluginsArgs`,
    `ListListenersArgs`, `ListActionsArgs` — all declared,
    none consumed. Treat as serialization-contract documentation,
    not dead code. No change needed.

14. **Plan §"References" → Existing code mentions reusing
    `ExtensionPointInspector.readField` / `readMethod`.**
    ServiceInventory instead duplicates both helpers
    (`ServiceInventory.kt:161-184`). The implementations are
    byte-for-byte identical to `ExtensionPointInspector:569-592`.
    The plan even called out "extract them to `core/internal/
    Reflect.kt` if cross-file access is desired" as the cleaner
    alternative. Recommend the extraction now — currently three
    inspectors (`ExtensionPointInspector`, `ServiceInventory`,
    `ListenerInspector` via `ExtensionPointInspector.readField`)
    use these helpers from two distinct copies. Two-line extraction;
    eliminates the chance of a fix landing on one copy only.

15. **Plan §"Edge cases" #10 — empty result returns
    `ListServicesResponse([], 0)`.** Confirmed at
    `ArchitectureToolset.kt:574` (`all.take(limit), all.size`).
    Correct.

### Cross-cutting

16. **[INFO] `PluginDetails` is not extended to include services.**
    The plan explicitly scoped this out ("Tool-window integration —
    once `Snapshot` carries services, the Platform Explorer tool
    window can add a `Services` tab in a follow-up. Out of scope
    here."). Confirmed — `arch.get_plugin_details` returns the same
    `PluginInfo + declaredEps + extensions + actions` shape. Future
    PR territory. No change needed in this PR.

17. **[INFO] `arch_list_services` does not validate `scope`.** Per
    the plan ("permissive style — matches existing
    `arch.list_extension_points`"), invalid values like
    `scope="foo"` fall through the filter and yield zero results
    rather than throwing. Sibling `arch.list_listeners` chose the
    strict route (`VALID_LISTENER_SCOPES` set + `McpExpectedError`).
    Either is defensible; flagging only because the two `arch.*`
    tools in the same file now disagree on validation strategy.

## Research notes

- **`ServiceDescriptor` field surface** confirmed via
  `intellij-community/.../components/ServiceDescriptor.java`. Public
  final Java fields: `serviceInterface`, `serviceImplementation`,
  `testServiceImplementation`, `headlessImplementation`, `overrides`,
  `open`, `configurationSchemaKey` (`@ApiStatus.Internal`),
  `preload: PreloadMode` (`@ApiStatus.Internal`), `client: ClientKind?`,
  `os`. Means: every `serviceInfoOf` getter probe is wasted work in
  the modern build; field-first probing is the unambiguous fast path.
- **`PreloadMode` enum values**: `TRUE`, `FALSE`, `AWAIT`, `NOT_HEADLESS`,
  `NOT_LIGHT_EDIT`. Exactly matches the strings in
  `ServiceInfo.preload`'s kdoc and the `@McpDescription` Returns
  section. Stable.
- **`ContainerDescriptor.services`** is declared
  `@JvmField val services: List<ServiceDescriptor>` — see finding 2.
  Field-first probing is faster.
- **`IdeaPluginDescriptorImpl` container fields** are plain Kotlin
  `val` properties (no `@JvmField`): Kotlin synthesises
  `getAppContainerDescriptor()`, `getProjectContainerDescriptor()`,
  `getModuleContainerDescriptor()`. Means: getter-first probe in
  `readContainerDescriptor` is the right call. No legacy `app` /
  `project` aliases visible in current master, but
  `ListenerInspector`'s in-code comment asserts earlier 251.x builds
  had them — see finding 1.
- **`@Service` light services** are auto-discovered via annotation
  scanning per `plugins.jetbrains.com/docs/intellij/plugin-services.html`
  and do NOT appear in `containerDescriptor.services`. The
  `@McpDescription` warning is correct; the plan's open question 4
  is resolved correctly as "out of scope until demand arises".

## References

- `ServiceDescriptor.java` (master):
  https://github.com/JetBrains/intellij-community/blob/master/platform/core-api/src/com/intellij/openapi/components/ServiceDescriptor.java
- `ContainerDescriptor.kt` (master):
  https://github.com/JetBrains/intellij-community/blob/master/platform/core-impl/src/com/intellij/ide/plugins/ContainerDescriptor.kt
- `IdeaPluginDescriptorImpl.kt` (master):
  https://github.com/JetBrains/intellij-community/blob/master/platform/core-impl/src/com/intellij/ide/plugins/IdeaPluginDescriptorImpl.kt
- Plugin Services docs (light vs registered):
  https://plugins.jetbrains.com/docs/intellij/plugin-services.html
- Sibling pattern (`ListenerInspector.kt:62-70`) — the legacy
  field-name fallback that finding 1 wants ported here.
- CLAUDE.md "Common pitfalls" — explicitly mentions the
  containerDescriptor rename as a 252 hazard.
- Plan: `/home/user/ide-introspector-plugin/docs/plans/arch-list-services.md`
- Implementation commit: `1460c06 feat(arch): salvage arch.list_services (untested)`.
