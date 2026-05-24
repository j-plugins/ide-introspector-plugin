# `arch.list_services`

## Purpose & motivation

Enumerates every IntelliJ **service** (application / project / module-scoped) declared
across all loaded plugins. Services are the third pillar of the IntelliJ plugin model
alongside extension points and listeners; today the IDE Introspector covers EPs
(`arch.list_extension_points`, `arch.list_extensions_for_ep`) but has no first-class
lens on services. JetBrains' built-in MCP server also has nothing for services, so
an agent investigating "what services does plugin X register?" or "where is the
implementation of `MyAppService`?" has to read `plugin.xml` by hand.

This tool mirrors `arch.list_extension_points`: an application-level cached inventory,
a `core/ServiceInventory.kt` headless class, a `@Serializable` response, and a thin
`@McpTool` wrapper on `ArchitectureToolset`.

**Success criterion**: an agent can answer "list every project-scoped service
contributed by `org.jetbrains.kotlin`" or "which plugin owns the implementation of
service `com.intellij.psi.PsiManager`?" in a single MCP call, without instantiating
any service.

## Tool specification

### `arch.list_services`

**Signature:**

```kotlin
@McpTool(name = "arch.list_services")
@McpDescription("""…see below…""")
suspend fun arch_list_services(
    @McpDescription("Service scope filter: 'application', 'project', 'module', or 'all'. Default 'all'.")
    scope: String = "all",
    @McpDescription("Restrict to services contributed by this plugin id (e.g. 'com.intellij', 'org.jetbrains.kotlin').")
    providedByPluginId: String? = null,
    @McpDescription("Case-insensitive substring filter on serviceInterface OR serviceImplementation FQN. Strongly recommended — IDEA ships 1000+ services.")
    nameContains: String? = null,
    @McpDescription("Include services with preload != FALSE only. Default false.")
    onlyPreloaded: Boolean = false,
    @McpDescription("Cap on returned services. Default 500.")
    limit: Int = 500,
): ListServicesResponse
```

**`@McpDescription` draft** (copy-paste verbatim — keep `""" |line… """` trim-margin
format the reflection bridge expects):

```
|Enumerates IntelliJ services (application/project/module-scoped) declared by every
|loaded plugin. Per service: serviceInterface FQN, serviceImplementation FQN, scope,
|preload mode, test/headless/overriding implementations, and the contributing plugin id.
|Reads from PluginDescriptor.containerDescriptor.services — never instantiates a service
|(safe even for half-broken third-party plugins).
|
|Use this when: a user asks "what services does plugin X expose?", "where is the
|implementation of service Y?", "which services are application-level vs project-level?",
|"what preloads at startup?" — i.e. service-layer plugin-architecture exploration. This
|is the service-shaped counterpart of arch.list_extension_points.
|
|Follow-up tools:
|  - arch.get_plugin_details      — full inventory for one plugin (EPs + extensions)
|  - arch.list_extension_points   — for non-service extensibility hooks
|
|Do NOT use this when: you want extension points (use arch.list_extension_points),
|plugin metadata (arch.list_plugins / arch.get_plugin_details), or actions
|(arch.list_actions). Service implementation classes are NOT auto-instantiated by this
|tool: do not use it to "fetch" a service instance. @Service-annotated light services
|registered without plugin.xml are NOT enumerated here (see arch.find_extenders_of).
|
|Returns: { services: ServiceInfo[], total: int } where each ServiceInfo has
|serviceInterface (FQCN; equals serviceImplementation when XML omits the interface),
|serviceImplementation (FQCN), scope ('application'|'project'|'module'), preload
|('FALSE'|'TRUE'|'NOT_HEADLESS'|'NOT_LIGHT_EDIT'|'AWAIT'), overrides (boolean),
|testServiceImplementation (FQCN or null), headlessImplementation (FQCN or null),
|providedByPluginId, providedByPluginName.
|
|Vanilla IDEA Community has 1000+ services — narrow with nameContains ("Psi", "Project",
|"Editor") and/or providedByPluginId before reading, or rely on limit (default 500).
|
|Examples:
|  scope="application", nameContains="Psi"                   — application-scoped PSI services
|  providedByPluginId="org.jetbrains.kotlin"                 — every service the Kotlin plugin registers
|  scope="project", onlyPreloaded=true                       — project services that preload eagerly
|  nameContains="ToolWindow", scope="all"                    — services matching ToolWindow at any scope
```

**Args** — every parameter, type, default, validation:

- `scope: String = "all"` — `"application" | "project" | "module" | "all"`. Invalid
  values fall through the filter (no match) rather than throwing — matches existing
  `arch.list_extension_points` permissive style.
- `providedByPluginId: String? = null` — exact match on `PluginId.idString`.
- `nameContains: String? = null` — case-insensitive substring matched against BOTH
  `serviceInterface` AND `serviceImplementation` so the caller doesn't need to know
  which one their query string belongs to.
- `onlyPreloaded: Boolean = false` — keep services whose `ServiceDescriptor.preload`
  is anything other than `PreloadMode.FALSE`.
- `limit: Int = 500` — same rationale as `arch.list_extension_points`'s 500;
  IDEA Ultimate has ~1500 services, Community ~1000.

**Response model** — in `model/ServiceInfo.kt`:

```kotlin
@Serializable
data class ServiceInfo(
    /** Service interface FQN. When XML omits the attribute, equals [serviceImplementation]. */
    val serviceInterface: String,
    /** Concrete implementation FQN. Always present. */
    val serviceImplementation: String,
    /** "application" | "project" | "module". */
    val scope: String,
    /** Raw PreloadMode name: "FALSE" (default), "TRUE", "NOT_HEADLESS", "NOT_LIGHT_EDIT", "AWAIT". */
    val preload: String = "FALSE",
    /** ServiceDescriptor.overrides — true when this declaration replaces a previously-registered service. */
    val overrides: Boolean = false,
    val testServiceImplementation: String? = null,
    val headlessImplementation: String? = null,
    val providedByPluginId: String,
    val providedByPluginName: String?,
)

@Serializable
data class ListServicesResponse(
    val services: List<ServiceInfo>,
    val total: Int,
)
```

Args mirror in `model/args/ArchArgs.kt`:

```kotlin
@Serializable
data class ListServicesArgs(
    val scope: String = "all",
    val providedByPluginId: String? = null,
    val nameContains: String? = null,
    val onlyPreloaded: Boolean = false,
    val limit: Int = 500,
)
```

## IntelliJ APIs used

Canonical type: `com.intellij.openapi.components.ServiceDescriptor`
([source](https://github.com/JetBrains/intellij-community/blob/master/platform/core-api/src/com/intellij/openapi/components/ServiceDescriptor.java)).
Relevant fields (all public on the descriptor instance):

- `serviceInterface: String?` — interface FQN; may be null when only
  `serviceImplementation` was declared.
- `serviceImplementation: String?` — impl FQN; always present for a valid declaration.
- `testServiceImplementation: String?` — test-only override.
- `headlessImplementation: String?` — headless override.
- `overrides: Boolean` — replaces a previously-registered service at the same key.
- `preload: PreloadMode` enum (FALSE / TRUE / NOT_HEADLESS / NOT_LIGHT_EDIT / AWAIT).
  Read its `.name` for the string form.

ServiceDescriptors live on `IdeaPluginDescriptorImpl`'s container descriptors:

- `appContainerDescriptor.services` — application-scoped descriptors.
- `projectContainerDescriptor.services` — project-scoped.
- `moduleContainerDescriptor.services` — module-scoped.

**Stability caveat**: `IdeaPluginDescriptorImpl` is `@ApiStatus.Internal`
([source](https://github.com/JetBrains/intellij-community/blob/master/platform/core-impl/src/com/intellij/ide/plugins/IdeaPluginDescriptorImpl.kt)),
and the container descriptors' `services` field is package-private in some platform
builds and public in others. Mirror the `ExtensionPointInspector.extractAllEps` /
`ExtensionMetadata` pattern: probe by reflection through the public
`PluginDescriptor` interface, never cast to the impl class. Reuse the existing
hierarchy-walking `readField` / `readMethod` helpers (extract them to
`core/internal/Reflect.kt` if cross-file access is desired, or keep them public-internal
where they live).

**Critical: do not instantiate services.** The exact analog of the
`ep.extensionList.size` vs `ep.size()` pitfall from CLAUDE.md. For services, the
no-instantiation path is "read the `ServiceDescriptor` POJO". Never call
`project.getService(...)` or `componentManager.getServiceIfCreated(...)` from this
tool — that would force-load every service, triggering startup-activity-style
failures in third-party plugins. `ServiceDescriptor` is a data carrier; reading its
fields is free.

References:
- ContainerDescriptor: https://github.com/JetBrains/intellij-community/blob/master/platform/core-impl/src/com/intellij/ide/plugins/ContainerDescriptor.kt
- Plugin model overview: https://plugins.jetbrains.com/docs/intellij/plugin-services.html

## Threading & EDT model

`arch.*` tools do **not** need EDT. ServiceDescriptor enumeration is a pure read of
already-materialized PluginDescriptor fields — same threading guarantee as
`ExtensionPoint` enumeration ("`arch.*` tools don't need EDT bouncing" per CLAUDE.md).
No PSI, no Swing, no VFS. The `suspend fun` runs on the ktor coroutine and reads the
inventory directly.

**Cache**: extend `PluginInventory.Snapshot` with `services: List<ServiceInfo>`,
populated in `collect()`. Services rarely change at runtime — declared in plugin.xml,
only flipping on dynamic plugin load/unload. The existing 30 s
`PluginInventory.CACHE_TTL_MS` is appropriate. Single cache means the Platform
Explorer tool window can later add a "Services" tab from the same source of truth.
(Alternative — standalone `ServiceInventory` with its own `TtlCache` — is simpler
to diff but means two caches to keep coherent; rejected.)

## Timeout strategy

Hard 10 s cap per project rule. ServiceDescriptor enumeration is O(plugins × services
per plugin) ≈ 1500 in-memory field reads in Ultimate — well under 100 ms. No bouncing,
no IO, no classloading. The `limit` arg and `nameContains` / `providedByPluginId`
filters cap response size; collection itself is unconditionally cheap and runs inside
the 30 s `TtlCache` so repeated calls hit cached data.

Wrap each descriptor's enumeration in `runCatching { … }.getOrElse { emptyList() }`
(mirroring `ExtensionPointInspector.collectFromArea`) so one corrupt third-party
plugin does not poison the whole result.

## Edge cases

1. **`serviceInterface` is null** — happens when plugin.xml declares only
   `serviceImplementation="..."`. Fall back to `serviceImplementation` so callers
   always see a non-null interface FQN.
2. **`serviceImplementation` is null** — malformed declaration. Skip with a debug
   log; don't emit a `ServiceInfo` with a null impl.
3. **Disabled plugins** — `PluginManagerCore.plugins` returns enabled descriptors
   only; their services are not registered at runtime, so correct to omit. "Include
   disabled" would be a follow-up.
4. **Dynamic plugin unloads mid-call** — cached snapshot may reference a no-longer-
   loaded plugin. Acceptable for read-only data; next TTL refresh drops it.
5. **Broken third-party service impl** — analogous to the EP `extensionList` pitfall.
   We **never** call `project.getService(...)`, so a broken `<init>` cannot trip us;
   we just emit its FQN string. Verified safe.
6. **Module-scoped services** — `moduleContainerDescriptor.services` exists on every
   descriptor but is rarely populated. Include for completeness; real-world counts
   <50. (See open questions.)
7. **`preload` API drift** — older builds expose `preload: Boolean`, newer expose
   `preload: PreloadMode`. Reflective read with a `when` on runtime type, defaulting
   to `"FALSE"`, handles both.
8. **Inner-class implementations** — `MyOuter$MyInner` FQNs come through as-is; no
   special handling.
9. **Test fixture** — `ApplicationManager.getApplication()` returns a `MockApplication`
   in unit tests; its `ExtensionsArea` is empty but `PluginManagerCore.plugins`
   still works. Mirrored by `PluginInventoryPlatformTest`.
10. **Empty result** — when filters exclude everything, return
    `ListServicesResponse([], 0)`. Do not throw.

## Files to create/modify

| Path | Op | What |
|------|----|------|
| `tools/ArchitectureToolset.kt` | Edit | Add `arch_list_services` method (≈20 LOC) |
| `core/ServiceInventory.kt` | Create | Headless enumeration — pure POJO reflection, no IDE-runtime deps beyond PluginDescriptor |
| `core/PluginInventory.kt` | Edit | Extend `Snapshot` with `services: List<ServiceInfo>`; populate in `collect()`; expose `services()` + `servicesByPlugin(id)` |
| `model/ServiceInfo.kt` | Create | `@Serializable` `ServiceInfo` + `ListServicesResponse` |
| `model/args/ArchArgs.kt` | Edit | Append `ListServicesArgs` |
| `src/test/kotlin/.../core/ServiceInventoryReflectionTest.kt` | Create | Unit tests with synthetic `PluginDescriptor` / `ServiceDescriptor` POJOs — mirror `ExtensionPointInspectorReflectionTest` |
| `src/test/kotlin/.../core/platform/ServiceInventoryPlatformTest.kt` | Create | `LightPlatformTestCase` smoke tests against the running sandbox IDE |

No XML wiring needed — services live in the existing `arch.*` group already
registered by `META-INF/mcp-integration.xml` via `ArchitectureToolset`. No new
optional dependency.

## Test plan

**Unit (`ServiceInventoryReflectionTest.kt`)** — pure JVM, no IntelliJ runtime,
synthetic-stub pattern from `ExtensionPointInspectorReflectionTest`:

- reads `serviceInterface` and `serviceImplementation` from a synthetic
  `ServiceDescriptor`-shaped POJO.
- falls back to `serviceImplementation` when `serviceInterface` is null.
- skips entries with null `serviceImplementation`.
- reads `preload` as `PreloadMode.name` when present.
- reads `preload` as `Boolean` for older platform builds.
- surfaces `overrides` flag.
- swallows exceptions on a single bad descriptor and continues.
- filters by `scope` / `providedByPluginId` / `nameContains` correctly.
- `nameContains` matches against BOTH interface and implementation.

**Platform (`ServiceInventoryPlatformTest.kt`)** — extends `LightPlatformTestCase`,
mirrors `PluginInventoryPlatformTest`:

- `testServicesListIsNonEmpty` — sandbox IDE has ≥100 services.
- `testServicesIncludeApplicationScope` — at least one with `scope == "application"`.
- `testServicesIncludeProjectScope` — at least one with `scope == "project"`.
- `testKnownPlatformServicePresent` — hard-code one stable FQN after first run
  (candidate: `com.intellij.openapi.fileEditor.FileEditorManager`).
- `testNoneInstantiatedAfterEnumeration` — guarded: pick a known lazy service
  (e.g. `PsiManager`) and verify `getServiceIfCreated` is still null after our
  enumeration (best-effort — may be flaky if test ordering touches it; document
  and `assumeTrue` if so).
- `testCacheRefreshProducesFreshServicesList` — same TTL behaviour as plugin/EP
  caches.

Filter behaviour is covered by unit tests; the toolset wrapper is thin enough not
to need its own dedicated test class.

## Estimated effort

| Step | Hours |
|------|-------|
| `ServiceInfo` model + args | 0.5 |
| `ServiceInventory.kt` reflective collection | 2.5 |
| `PluginInventory.Snapshot` extension + cache wiring | 1.5 |
| `arch_list_services` toolset method + `@McpDescription` | 1 |
| Unit tests (synthetic descriptors) | 1.5 |
| Platform tests | 1 |
| Doc-gen verification + manual `runIde` smoke | 0.5 |
| **Total** | **~1 day** |

## Open questions / risks

1. **Module scope** — `moduleContainerDescriptor.services` is rarely populated and
   most callers won't care. **Decision: include for completeness** (trivially cheap);
   document in the description that real-world counts are tiny.
2. **Preload surface** — surface the raw enum name (`"FALSE"`, `"TRUE"`,
   `"NOT_HEADLESS"`, `"NOT_LIGHT_EDIT"`, `"AWAIT"`) rather than a boolean. The names
   are stable platform contract and matter for an agent diagnosing startup cost. The
   `onlyPreloaded` filter treats anything-but-`"FALSE"` as preloaded.
3. **`overrides` semantics** — we surface the flag but don't resolve which earlier
   declaration was replaced; that would require sorting plugin descriptors by
   load-order. Possible follow-up: `arch.get_service_details` with an
   `overridingChain` field.
4. **Light services** — `@Service`-annotated classes registered without plugin.xml
   do NOT appear in `containerDescriptor.services`. Note this limitation in the
   `@McpDescription`. Scanning the classpath for `@Service` annotations would be
   heavy and slow — defer to a separate `arch.list_light_services` tool if demand
   arises.
5. **Tool-window integration** — once `Snapshot` carries services, the Platform
   Explorer tool window can add a "Services" tab in a follow-up. Out of scope here.

## References

- Existing code:
  - `tools/ArchitectureToolset.kt#arch_list_extension_points` — same shape (filter +
    cap + cached read).
  - `core/PluginInventory.kt#collect` — extend Snapshot the same way.
  - `core/ExtensionPointInspector.kt#extractAllEps` — reflection pattern to mirror.
  - `core/internal/ExtensionMetadata.kt` — pure-JVM helper layout for unit tests.
- IntelliJ source:
  - `ServiceDescriptor`: https://github.com/JetBrains/intellij-community/blob/master/platform/core-api/src/com/intellij/openapi/components/ServiceDescriptor.java
  - `ContainerDescriptor`: https://github.com/JetBrains/intellij-community/blob/master/platform/core-impl/src/com/intellij/ide/plugins/ContainerDescriptor.kt
  - `IdeaPluginDescriptorImpl` (internal — use reflection): https://github.com/JetBrains/intellij-community/blob/master/platform/core-impl/src/com/intellij/ide/plugins/IdeaPluginDescriptorImpl.kt
- JetBrains MCP equivalent: **none**. Greenfield in the agent-tooling space.
