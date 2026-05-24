# `arch.list_actions`

## Purpose & motivation

A flat, query-filterable, paginated catalog of every `AnAction` registered in the
running IDE — the JSON equivalent of `Ctrl+Shift+A` "Find Action". The existing
`arch.get_plugin_details(includeActions=true)` only walks ONE plugin's actions and
intentionally defaults `includeActions=false` because instantiation is slow (see
`ArchitectureToolset.actionsFor` at
`/home/user/ide-introspector-plugin/src/main/kotlin/com/github/xepozz/ide/introspector/tools/ArchitectureToolset.kt:253`).
There is no cross-plugin action search today.

Vs. JetBrains' built-in MCP server: the old OSS `ListAvailableActionsTool` loaded
every action eagerly (the known slow path). Our value-add is rich metadata
(plugin owner, formatted shortcut, action-group category, internal-flag, icon
path) plus a registry-level prefix path that avoids instantiation when the query
is a literal prefix.

**Success criterion:** an agent can answer "what action invokes Refactor → Rename
and which plugin owns it?" in one MCP call without parsing keymaps or scraping
docs.

## Tool specification

### `arch.list_actions`

**Signature:**
```kotlin
@McpTool(name = "arch.list_actions")
@McpDescription("…verbatim block below…")
suspend fun arch_list_actions(
    @McpDescription("…") query: String? = null,
    @McpDescription("…") providedByPluginId: String? = null,
    @McpDescription("…") includeInternal: Boolean = false,
    @McpDescription("…") limit: Int = 200,
): ListActionsResponse
```

**`@McpDescription` (verbatim, trim-margin):**
```
|Global action catalog with fuzzy search — the JSON equivalent of Ctrl+Shift+A "Find
|Action" across every plugin. Each result carries id, display text, description,
|owning plugin id/name, formatted keyboard shortcut(s), the action-group path
|(category), the icon path, and an isInternal flag.
|
|Use this when: you need to discover an action id to call later, find which plugin
|owns a feature ("who provides 'Run Anything'?"), audit shortcut bindings, or list
|all actions a specific plugin contributes. Pairs with arch.get_plugin_details for
|the inverse direction (plugin → its actions).
|
|Do NOT use this when: you want to *invoke* an action (use ui.invoke_action_on or the
|JetBrains built-in execute_action_by_id), or you already have a plugin id and want
|that plugin's full inventory (arch.get_plugin_details(includeActions=true)). Do not
|use this to enumerate action *groups* — groups are out of scope here, query their
|child action ids instead.
|
|Returns: { actions: ActionInfo[], total: int, truncated: bool }. `total` is the
|count BEFORE applying `limit`; `truncated=true` means either the limit was hit OR
|the 10 s hard timeout fired and the catalog walk stopped early.
|
|Performance: vanilla IDEA has ~3000+ registered actions. When `query` looks like a
|prefix (alphanumeric, no wildcards), the lookup uses ActionManagerEx.getActionIdList
|at the registry level (no AnAction instantiation). Otherwise we stream the full id
|list applying filters lazily and stop at `limit`. Results are cached in a TtlCache
|keyed on (query, providedByPluginId, includeInternal) for 60 s. Even with these
|optimisations, prefer a non-null `query` whenever possible — an unfiltered call
|with limit=200 still walks every id.
|
|Examples:
|  query="Refactor"                              — every action whose id or text contains "Refactor"
|  query="Editor", providedByPluginId="com.intellij" — platform editor actions only
|  providedByPluginId="org.jetbrains.kotlin"      — every Kotlin-plugin action
|  query="Run", includeInternal=true, limit=500  — Run* actions, internal included
```

**Args:**

| Name | Type | Default | Validation / notes |
|------|------|---------|--------------------|
| `query` | `String?` | `null` | Case-insensitive substring on `id` + display text. Triggers prefix fast-path when `^[A-Za-z0-9_.]+$`. |
| `providedByPluginId` | `String?` | `null` | Exact match on plugin id (`com.intellij`, `org.jetbrains.kotlin`, …). |
| `includeInternal` | `Boolean` | `false` | Include actions with `@ApiStatus.Internal` or `internal=true` attribute on `<action>`. End-user noise by default. |
| `limit` | `Int` | `200` | Hard upper bound 2000. Coerced into `1..2000`. |

**Response models** (`model/ActionInfo.kt`, new):
```kotlin
@Serializable data class ListActionsResponse(
    val actions: List<ActionInfo>,
    val total: Int,
    val truncated: Boolean,
)

@Serializable data class ActionInfo(
    val id: String,
    val text: String?,
    val description: String?,
    val providedByPluginId: String?,
    val providedByPluginName: String?,
    val keyboardShortcuts: String? = null, // "Ctrl+S, Shift+Alt+F" or null
    val category: String? = null,          // action group path, e.g. "Main Menu/Refactor"
    val iconPath: String? = null,
    val isInternal: Boolean = false,
)
```

## IntelliJ APIs used

- `com.intellij.openapi.actionSystem.ActionManager.getInstance()` — application service.
- `com.intellij.openapi.actionSystem.ex.ActionManagerEx.getInstanceEx()` —
  - `getActionIdList(prefix: String)` — registry-level enumeration; empty prefix
    = all. NO `AnAction` instantiation.
  - `getPluginActions(pluginId)` — alternate enumeration when filtering by plugin.
- `actionManager.getAction(id)` — lazy load (may trigger class loading). Used
  only after registry filtering whittles the candidate set.
- `actionManager.getId(action)` / `getActionOrStub(id)` — `getActionOrStub` returns
  the stub without triggering full class load when present.
- `com.intellij.openapi.keymap.KeymapManager.getInstance().activeKeymap.getShortcuts(id)`
  + `com.intellij.openapi.keymap.KeymapUtil.getShortcutText(shortcut)` — formatted
  human-readable text.
- Plugin owner: walk every `PluginDescriptor` and check `getPluginActions(pluginId)`
  membership — already done in `ArchitectureToolset.actionsFor`. Build a reverse
  `Map<String, PluginDescriptor>` once per cache entry.
- Internal flag: `com.intellij.openapi.actionSystem.AnAction.isInternal()` (delegates
  to `<action internal="true"/>`).

Stability notes:
- `ActionManagerEx` is `@ApiStatus.Internal` but stable across recent versions
  and already used in this codebase (line 253 of `ArchitectureToolset.kt`).
- `getActionOrStub` is internal but exposed via reflection if needed.

## Threading & EDT model

- `ActionManager` queries (`getActionIdList`, `getPluginActions`) are thread-safe
  and do NOT require EDT.
- `KeymapManager.getInstance().activeKeymap` is also thread-safe per platform docs;
  shortcut lookup can happen off-EDT. **Verify in a platform test** — if it asserts
  EDT, wrap only the shortcut-lookup phase in `onEdtBlocking { … }` with
  `ModalityState.any()`.
- `actionManager.getActionOrStub(id)` reads from an internal map — safe off-EDT.
  `getAction(id)` can trigger class loading; restrict to the candidate set after
  filtering, never the full 3000 ids.
- Cache: `TtlCache<ListActionsResponse>` keyed on a `data class ActionsCacheKey`
  (query, pluginId, includeInternal). 60 s TTL (actions change only on plugin
  install/disable). Single per-application instance in a new
  `ActionInventory` service.

## Timeout strategy

- Hard 10 s cap. Implementation uses `kotlinx.coroutines.withTimeoutOrNull(10_000)`
  around the walk. If timeout fires mid-walk, return the accumulated list with
  `truncated=true` and `total=accumulated.size` (we cannot know the would-be total).
- Prefix fast-path (`getActionIdList(prefix)`) typically returns under 100 ms for a
  3-char prefix even on heavy installs.
- Unfiltered walk (no query, no plugin filter, `limit=200`) measured target: < 2 s
  because we only call `getActionOrStub` (no class load) for the first 200 ids.
- Worst case (no query + `includeInternal=true` + `limit=2000`): expected under
  5 s; if it exceeds 10 s, surface `truncated=true` rather than raising the cap.

## Edge cases

1. **Action id present, action null** — `getAction(id)` returns null for stubs that
   failed to load. Emit `ActionInfo` with `text=null, description=null`,
   `keyboardShortcuts=null`, mark `category=null`.
2. **Action with no plugin owner** — built-in IDE actions sometimes have no
   discoverable plugin via `getPluginActions` reverse map (registered by core,
   not a PluginDescriptor). Set `providedByPluginId=null, providedByPluginName=null`
   rather than the string `"unknown"` (consistency with our other models).
3. **Multiple shortcuts** — `getShortcuts` returns an array. Format as
   comma-separated via `joinToString(", ") { KeymapUtil.getShortcutText(it) }`.
   Empty array → null (not empty string).
4. **Internal actions** — `<action internal="true">` are filtered out unless
   `includeInternal=true`. Detect via `AnAction.isInternal()` after `getAction`,
   or via XML attribute at registry level when stub is available.
5. **Project-mode actions** — some actions are registered conditionally per
   project; we list them all at app-level (matches Ctrl+Shift+A behaviour).
6. **`@ApiStatus.Experimental` keymap APIs** — wrapped in `runCatching` so a
   single failing shortcut lookup does not abort the whole walk.
7. **Action group path (`category`)** — derived by walking
   `ActionManager.getActions(...)` for groups containing the id. Expensive to
   compute exhaustively; restrict to first-hit ancestor and cache. If detection
   is > 50 ms per id, default `category=null` and add an opt-in arg in a later
   iteration.
8. **Dumb mode / project null** — application-level, unaffected.
9. **Plugin disabled mid-walk** — caught via `runCatching`; entry omitted.
10. **Duplicate ids across keymaps** — registry guarantees uniqueness; not an
    issue.

## Files to create/modify

| Path | Op | What |
|------|----|------|
| `src/main/kotlin/com/github/xepozz/ide/introspector/tools/ArchitectureToolset.kt` | Edit | Add `@McpTool fun arch_list_actions(...)` calling `ActionInventory`. |
| `src/main/kotlin/com/github/xepozz/ide/introspector/core/ActionInventory.kt` | Create | App-level `@Service` with `TtlCache`, prefix-vs-substring routing, plugin-owner reverse map, shortcut formatter, 10 s `withTimeoutOrNull`. |
| `src/main/kotlin/com/github/xepozz/ide/introspector/model/ActionInfo.kt` | Create | `@Serializable ListActionsResponse` + `ActionInfo`. |
| `src/main/kotlin/com/github/xepozz/ide/introspector/model/args/ArchArgs.kt` | Edit | Optional `ListActionsArgs` if we standardise on arg classes (currently inline args are used in `ArchitectureToolset`, keep consistent). |
| `src/test/kotlin/com/github/xepozz/ide/introspector/core/ActionInventoryTest.kt` | Create | Unit: shortcut formatter, prefix-detection regex, cache key equality. |
| `src/test/kotlin/com/github/xepozz/ide/introspector/core/platform/ActionInventoryPlatformTest.kt` | Create | `BasePlatformTestCase` — see test plan. |

No new META-INF wiring (lives in the always-loaded `mcp-integration.xml` via the
existing `ArchitectureToolset` registration).

## Test plan

**Unit (`ActionInventoryTest`):**
- `isPrefixQuery("EditorCopy") == true`, `isPrefixQuery("Editor*") == false`,
  `isPrefixQuery("a b") == false`, `isPrefixQuery(null) == false`.
- `formatShortcuts(emptyArray()) == null`.
- Cache key: equal args produce equal hash/equals.

**Platform (`ActionInventoryPlatformTest extends BasePlatformTestCase`):**
- `list_actions(query="EditorCopy")` returns ≥1 entry whose `id == "EditorCopy"`
  and `providedByPluginId == "com.intellij"`.
- `list_actions(query="RunAction")` returns an entry whose `keyboardShortcuts`
  is non-null and contains "Shift" (best-effort — guarded under default keymap
  available check).
- `list_actions(providedByPluginId="com.intellij", limit=10)` returns exactly
  10 entries with `truncated=true`.
- `list_actions(query="$$$nonexistent$$$")` returns `actions=[], total=0,
  truncated=false`.
- `list_actions(includeInternal=false)` excludes a known `<action internal="true">`
  id (pick one from the platform, e.g. one starting with `Internal.`).
- Same query called twice within 60 s — second call hits cache (assert via a
  spy or by measuring object identity if we keep the same list instance).

## Estimated effort

~1.5 days:
- 0.25 d — `ActionInfo` model + arg wiring + `@McpDescription` text.
- 0.5 d — `ActionInventory` core: prefix detection, reverse plugin map, shortcut
  formatting, TtlCache, timeout.
- 0.25 d — `ArchitectureToolset` integration.
- 0.5 d — tests (unit + platform) + perf tuning on a real install to confirm
  the unfiltered call stays under 2 s.

## Open questions / risks

- **Action groups in the response?** Probably no — out of scope per the brief.
  Groups have different semantics (containers vs. invokable verbs) and would
  blow up the response shape. If demand surfaces, add a separate
  `arch.list_action_groups` rather than mixing.
- **`KeymapManager` EDT-sensitivity** — needs empirical verification in the
  platform test; if it does assert EDT, the shortcut-lookup phase moves into a
  single `onEdtBlocking` batch (still one EDT trip, not one per id).
- **`category` cost** — computing the action-group path for every entry may be
  prohibitive; ship with `category=null` if benchmarking shows > 50 ms/id, and
  iterate later behind an opt-in flag.
- **Overlap with JetBrains' future built-in** — if the new in-IDE MCP server
  adds an equivalent tool, our differentiator is the rich metadata
  (plugin/shortcut/category) plus the prefix fast-path. Worth keeping.

## References

- Existing similar code: `ArchitectureToolset.actionsFor` (lines 253–257) and
  `arch.get_plugin_details(includeActions=true)` for the per-plugin variant.
- Cache pattern: `core/internal/TtlCache.kt` + `core/internal/ExtensionMetadata.kt`
  consumer.
- IntelliJ source — `ActionManagerEx`:
  `https://github.com/JetBrains/intellij-community/blob/master/platform/platform-impl/src/com/intellij/openapi/actionSystem/ex/ActionManagerEx.kt`
- JetBrains MCP equivalent: the old OSS `ListAvailableActionsTool` in
  `https://github.com/JetBrains/mcp-server-plugin` — eagerly loaded all
  actions; we avoid that via the prefix fast-path + lazy stub lookup.
