# `arch.list_listeners`

## Purpose & motivation

Enumerates every MessageBus listener declared in `plugin.xml` under `<applicationListeners>`
or `<projectListeners>` across all loaded plugins. For each listener we surface the topic
class FQN, listener implementation FQN, scope (`application` | `project`), the
`activeInTestMode` / `activeInHeadlessMode` flags, and the contributing plugin id.

This fills a real gap: neither the IDE itself nor the JetBrains-shipped MCP server
(IntelliJ 2025.2+) exposes "who is listening to topic X?" anywhere. The Platform Explorer
in our own plugin doesn't cover it either. Plugin developers debugging "my
`FileEditorManagerListener.fileOpened` callback never fires" or "what other plugins react
to `BulkFileListener`?" currently have to grep the source tree of every installed plugin.

Success criterion: from one MCP call an agent can answer "which plugins subscribe to
`com.intellij.openapi.fileEditor.FileEditorManagerListener.TOPIC`?" and "what
application-scope listeners does plugin X register?".

## Tool specification

### `arch.list_listeners`

**Signature:**

```kotlin
@McpTool(name = "arch.list_listeners")
@McpDescription(
    """
    |Enumerates MessageBus listeners declared in plugin.xml across all loaded plugins
    |(both <applicationListeners> and <projectListeners>). For each declaration you get
    |the topic class FQN, the listener implementation FQN, its scope, the contributing
    |plugin id, and the test-mode / headless-mode activation flags.
    |
    |Use this when: a plugin developer wants to know "who is subscribed to topic X?",
    |"what listeners does plugin Y register?", or "why isn't my listener firing — is
    |something else consuming the event first?". Also useful for auditing
    |unexpected reactions to platform topics (BulkFileListener, FileEditorManagerListener,
    |DumbService.DUMB_MODE, VirtualFileManager.VFS_CHANGES, …).
    |
    |Do NOT use this when: you want listeners registered programmatically via
    |MessageBus.connect().subscribe(...) — those are NOT declared in plugin.xml and are
    |out of scope (see the platform's MessageBusImpl for that route). Also don't use
    |this to invoke or fire events — read-only inventory only.
    |
    |Returns: { listeners: ListenerInfo[], total: int }. Each ListenerInfo carries
    |topicClass (FQN), listenerClass (FQN), scope ('application'|'project'),
    |providedByPluginId, providedByPluginName, activeInTestMode, activeInHeadlessMode.
    |Vanilla IDEA Community has ~200-400 plugin.xml-declared listeners — narrow with
    |topicContains or providedByPluginId before reading the whole response.
    |
    |Examples:
    |  topicContains="FileEditorManagerListener"             — who watches file-editor events
    |  scope="project"                                       — project-scope listeners only
    |  providedByPluginId="org.jetbrains.kotlin"             — every listener the Kotlin plugin registers
    |  topicContains="VFS_CHANGES", scope="application"      — app-scope VFS subscribers
    """
)
suspend fun arch_list_listeners(
    @McpDescription("Case-insensitive substring filter on the topic class FQN (e.g. 'FileEditorManagerListener', 'VFS_CHANGES', 'BulkFileListener'). Strongly recommended — full list can be 400+.")
    topicContains: String? = null,
    @McpDescription("Restrict to listeners declared by this plugin id exactly (e.g. 'com.intellij', 'org.jetbrains.kotlin'). Get ids from arch.list_plugins.")
    providedByPluginId: String? = null,
    @McpDescription("'application', 'project', or 'both'. Default 'both'.")
    scope: String = "both",
    @McpDescription("Cap on returned listeners. Default 300.")
    limit: Int = 300,
): ListListenersResponse
```

**Args** — `model/args/ArchArgs.kt` (append to the existing file):

```kotlin
@Serializable
data class ListListenersArgs(
    val topicContains: String? = null,
    val providedByPluginId: String? = null,
    val scope: String = "both",          // "application" | "project" | "both"
    val limit: Int = 300,
)
```

Validation: `scope` outside `{application, project, both}` throws `McpExpectedError`
(consistent with how `arch_list_extension_points` silently accepts `area="both"`).
`limit` is clamped to `[1, 5_000]` to bound serialization cost.

**Response model** — new file `model/ListenerInfo.kt`:

```kotlin
@Serializable
data class ListenerInfo(
    val topicClass: String,              // FQN, e.g. "com.intellij.openapi.fileEditor.FileEditorManagerListener"
    val listenerClass: String,           // FQN of the user's listener implementation
    val scope: String,                   // "application" | "project"
    val providedByPluginId: String,      // "com.intellij" | "org.jetbrains.kotlin" | …
    val providedByPluginName: String?,
    val activeInTestMode: Boolean,
    val activeInHeadlessMode: Boolean,
    val os: String? = null,              // platform-specific filter from XML, if any
)

@Serializable
data class ListListenersResponse(
    val listeners: List<ListenerInfo>,
    val total: Int,
)
```

## IntelliJ APIs used

- `com.intellij.ide.plugins.PluginManagerCore.plugins` — entry point, identical to the
  one `PluginInventory.collect()` already iterates.
- Each entry is an `IdeaPluginDescriptor`; cast / reflect into `IdeaPluginDescriptorImpl`
  (in `com.intellij.ide.plugins`) to reach the package-private listener lists. Two fields:
  - `app: ContainerDescriptor` → its `.listeners: List<ListenerDescriptor>`
  - `project: ContainerDescriptor` → its `.listeners: List<ListenerDescriptor>`
- `com.intellij.ide.plugins.ListenerDescriptor` — fields `topicClassName`,
  `listenerClassName`, `activeInTestMode`, `activeInHeadlessMode`, optional `os`. All
  package-private; access via reflection.

**Stability note:** these are platform-internal types (`@ApiStatus.Internal` in
`intellij-community`). Field names have moved across versions: in older builds
`ContainerDescriptor` was an inner class on `IdeaPluginDescriptorImpl`; in current builds
it's a separate file in the same package. Both `topicClassName` and `listenerClassName`
have been stable since ~2019. We must reach them reflectively rather than via
`compileOnly` import to keep working across the 252.x / 253.x split.

**Reflective access** — reuse `ExtensionPointInspector.readField` (the same helper that
already pulls `XmlExtensionAdapter.extensionElement` and `ExtensionComponentAdapter.pluginDescriptor`).
The reachability chain looks like:

```kotlin
val app = readField(descriptor, "app")              // ContainerDescriptor (or null on non-Impl descriptors)
val appListeners = app?.let { readField(it, "listeners") } as? List<*>
for (ld in appListeners.orEmpty()) {
    val topic    = readField(ld!!, "topicClassName") as? String ?: continue
    val listener = readField(ld, "listenerClassName") as? String ?: continue
    val test     = (readField(ld, "activeInTestMode") as? Boolean) ?: true
    val headless = (readField(ld, "activeInHeadlessMode") as? Boolean) ?: true
    val os       = readField(ld, "os")?.toString()
    out += ListenerInfo(topic, listener, "application", pluginId, pluginName, test, headless, os)
}
// same for "project"
```

Cross-reference: this is the same reflective pattern already used at
`core/ExtensionPointInspector.kt:230` (`readField(adapter, "implementationClassOrName")`)
and `:233` (`readField(adapter, "pluginDescriptor")`). The CLAUDE.md "Common pitfalls"
section explicitly warns that `ExtensionComponentAdapter.pluginDescriptor` is a public
field — `ListenerDescriptor`'s fields are the same shape (Kotlin `val` backed by a JVM
field, no getter generated for package-private accessors).

Promote `readField` to `internal` visibility in `ExtensionPointInspector` (already is —
`internal fun readField`) and call it from the new `ListenerInspector`.

## Threading & EDT model

- Thread-safe: `PluginManagerCore.plugins` and `IdeaPluginDescriptorImpl` fields are
  immutable post-startup. Same model as `arch.list_extension_points`, which is documented
  as "no EDT bouncing needed" in CLAUDE.md ("Threading" section).
- No PSI / VFS reads. No `ReadAction`.
- The MCP `suspend fun` runs on the ktor coroutine; the inspector is a plain function call.

## Caching

- `TtlCache<List<ListenerInfo>>` with TTL = `60_000L` (60 s). Listeners only change when
  plugins reload (rare — full IDE restart in most cases, dynamic plugin load otherwise).
- Stored on the new `ListenerInspector` application service. Singleton via
  `@Service(Service.Level.APP)`, same pattern as `PluginInventory`.
- The Platform Explorer tool window's `Refresh` action should invalidate this cache too
  (follow-up — not blocking the MCP tool).

## Timeout strategy

- Worst case (vanilla IDEA Community + 30 third-party plugins): ~400 listener
  declarations, ~150 plugins to iterate. Reflective field reads only. Measured against
  `arch.list_extension_points`, which walks ~1500 EPs with similar reflection in <50 ms.
  No timeout wrapping needed.
- The `limit` default (300) is the safety net for serialization size, not for execution
  time. Hard cap at 5 000 in args validation.

## Edge cases

1. **Plugin descriptor is NOT an `IdeaPluginDescriptorImpl`** (custom IDE forks /
   test-time stubs). `readField(descriptor, "app")` returns `null`; skip the plugin and
   continue. Do NOT throw.
2. **`ContainerDescriptor` returned but its `listeners` field is `null`** (empty case in
   some platform versions — null is used instead of an empty list). Treat as empty.
3. **`ListenerDescriptor.topicClassName` or `listenerClassName` is `null`** — malformed
   XML that the platform silently tolerated. Skip the entry.
4. **Dynamic listeners** (`MessageBus.connect().subscribe(TOPIC, handler)`): NOT visible
   here. Tool description calls this out explicitly. See "Open questions" for the risky
   `MessageBusImpl.topicsBySubscriberMap` route.
5. **Disabled plugins**: `PluginManagerCore.plugins` returns enabled plugins by default;
   their listeners are not in effect anyway. We mirror `arch.list_plugins`' default
   (enabled-only) — no `includeDisabled` flag in v1. Add later if needed.
6. **Duplicate `topicClass + listenerClass + scope` rows across two plugins**: keep both;
   `providedByPluginId` differs, which is the diagnostic information the user wants.
7. **`activeInTestMode` / `activeInHeadlessMode` defaults** — when the XML attribute is
   absent, the platform defaults both to `true`. Mirror that default when the reflective
   read returns `null`.
8. **Field rename across platform versions** (e.g. some 251.x builds renamed `topicClassName`
   to `topicClass`). `readField` walks the class hierarchy and returns `null` on miss;
   if `null` for `topicClassName`, fall back to `topicClass` once, then skip.

## Files to create/modify

| Path | Op | What |
|------|----|------|
| `src/main/kotlin/com/github/xepozz/ide/introspector/tools/ArchitectureToolset.kt` | Edit | Add `arch_list_listeners` `@McpTool` method, delegating to `ListenerInspector.getInstance()` |
| `src/main/kotlin/com/github/xepozz/ide/introspector/core/ListenerInspector.kt` | Create | `@Service(Service.Level.APP)` class with `TtlCache<List<ListenerInfo>>` + reflective collector |
| `src/main/kotlin/com/github/xepozz/ide/introspector/model/ListenerInfo.kt` | Create | `ListenerInfo` + `ListListenersResponse` `@Serializable` types |
| `src/main/kotlin/com/github/xepozz/ide/introspector/model/args/ArchArgs.kt` | Edit | Append `ListListenersArgs` for symmetry with peers |
| `src/main/kotlin/com/github/xepozz/ide/introspector/core/ExtensionPointInspector.kt` | (no change) | `readField` already `internal`; new inspector imports it directly |
| `src/test/kotlin/com/github/xepozz/ide/introspector/core/ListenerInspectorReflectionTest.kt` | Create | Unit test for the field-name fallback logic using a hand-rolled stub descriptor |
| `src/test/kotlin/com/github/xepozz/ide/introspector/core/platform/ListenerInspectorPlatformTest.kt` | Create | Platform test extending `BasePlatformTestCase` — assert ≥1 known platform listener present (e.g. `FileEditorManagerListener` declared by `com.intellij`) |
| `docs/MCP_TOOLS.md` | (auto) | Regenerated by KSP — do not edit |

No new META-INF wiring: `ArchitectureToolset` is already registered by `mcp-integration.xml`.

## Test plan

**Unit (`ListenerInspectorReflectionTest`)** — JVM only, no IntelliJ runtime:

- Build a stub `Any` graph with `app.listeners = listOf(fakeListener("Topic1", "Impl1"))`
  using a Kotlin data class whose field names match the real `ContainerDescriptor` /
  `ListenerDescriptor`. Pass it through `ListenerInspector.collectFromDescriptor()` and
  assert the resulting `ListenerInfo`.
- Stub with `topicClassName = null` → entry skipped.
- Stub with `activeInTestMode` absent → defaults to `true`.
- Field-rename fallback: stub with only `topicClass` (no `topicClassName`) → still
  resolved.

**Platform (`ListenerInspectorPlatformTest`)** — `BasePlatformTestCase`:

- Run `ListenerInspector.getInstance().list()` against the actual test IDE.
- Assert `result.isNotEmpty()`.
- Assert at least one entry has `topicClass.contains("FileEditorManagerListener")` and
  `providedByPluginId == "com.intellij"`.
- Assert filter `scope = "application"` produces a strict subset (`<=` total).
- Assert filter `topicContains = "NoSuchTopicXYZ"` returns empty without throwing.

**Smoke** (manual via `runIde`): call the tool from a connected MCP client, eyeball that
counts roughly match a `grep -rE '<(application|project)Listeners>'` against
`<idea>/plugins/**/plugin.xml`.

## Estimated effort

- Inspector + reflection: 2 h
- Model + args + Toolset method + @McpDescription wording: 1.5 h
- Unit tests (stub descriptor scaffolding): 2 h
- Platform test (fixture-light, asserts against bundled platform listeners): 1 h
- Manual `runIde` smoke + tweaks: 1 h
- **Total: ~1 day** (7-8 h)

## Open questions / risks

1. **Should we also include programmatically-registered listeners by scraping
   `MessageBusImpl.topicsBySubscriberMap`?** Risky:
   - `MessageBusImpl` is `@ApiStatus.Internal`; the field name has churned (was
     `subscriberCache`, then `subscribers`, currently `topicsBySubscriberMap` on some
     branches). Reflective access is brittle.
   - `Application#getMessageBus()` is reachable but `Project#getMessageBus()` is per-project
     — we'd need a `Project` parameter, breaking the application-scope feel of `arch.*`.
   - Subscriber values are raw `Any` references to handlers; reverse-mapping them to a
     declaring plugin requires walking the classloader, which is the same trap that
     burned the `ExtensionPoint.extensionList.size` pitfall (CLAUDE.md).
   - Recommendation: **keep out of scope for v1.** Document the limitation in the tool
     description ("Do NOT use this for `MessageBus.connect()` subscriptions"). If the gap
     turns out to matter, ship a separate tool `arch.list_active_subscribers(projectId,
     topicClass)` whose scope and risks are clearly bounded.
2. **Multi-version stability of `IdeaPluginDescriptorImpl.app/project`**: reflective
   `readField` returning `null` is silent. Add a `thisLogger().debug` line on the first
   `null` per build so a verifier run surfaces the issue rather than producing an empty
   list quietly.
3. **`os` field** — present in some XML declarations (`os="mac"`, etc.). Including it
   for completeness; if it bloats the response without value, drop it in v1.1.
4. **No `includeDisabled` arg**: matches `arch.list_plugins` default. Add later if a real
   use case appears (e.g. "why doesn't my disabled-plugin's listener fire" — but the
   obvious answer is "because the plugin is disabled").

## References

- Existing reflective pattern: `core/ExtensionPointInspector.kt:262` (`readField` walks
  the class hierarchy) and `:230-237` (its consumers).
- CLAUDE.md "Common pitfalls" — the warning about `ExtensionComponentAdapter.pluginDescriptor`
  being a field, not a getter, applies verbatim to `ListenerDescriptor`.
- Sibling cache pattern: `core/PluginInventory.kt:29` (`TtlCache` with 30 s TTL); we use
  60 s here because listener data is even more static.
- JetBrains-shipped MCP server (IntelliJ 2025.2+) has **no equivalent tool**; closest
  built-in is `list_installed_plugins` which doesn't surface listener registrations.
- IntelliJ Community source (for reference, not a dependency):
  `platform/core-impl/src/com/intellij/ide/plugins/ListenerDescriptor.kt` and
  `platform/core-impl/src/com/intellij/ide/plugins/ContainerDescriptor.kt`.
