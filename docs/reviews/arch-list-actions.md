# Review: arch.list_actions

Branch: `claude/project-features-analysis-odEwP` @ HEAD (`3225444`)
Scope: `core/ActionInventory.kt`, `model/ActionInfo.kt`,
`model/args/ArchArgs.kt` (new `ListActionsArgs`), the new `@McpTool`
method on `tools/ArchitectureToolset.kt`, and
`src/test/kotlin/.../core/ActionInventoryTest.kt`.
Plan: `docs/plans/arch-list-actions.md`.

## Verdict

**Needs changes.** The shape is right — prefix fast-path is wired,
`TtlCache` is keyed on the full tuple, the 60 s TTL and 10 s wall-clock
budget are honoured, and `@McpDescription` follows the 5-section template.
But three correctness/perf issues land squarely on the perf-sensitive
hot path: (1) the no-query no-plugin path calls `resolveAction` for
**every one of 3000+ ids** to compute `isInternal`, killing the "only
resolve the first 200" promise in the plan; (2) the `isInternal` detector
checks the wrong annotation entirely — `@ApiStatus.Internal` is API-
stability metadata, not the `<action internal="true"/>` runtime
visibility flag that Ctrl+Shift+A actually filters on; and (3)
`resolveAction` does a full `am.javaClass.methods` reflection scan **per
id**, not once. The platform test promised in the plan was never
created.

## Findings

### Correctness

1. **[HIGH] `ActionInventory.kt:152-154` — `isInternalAction` checks
   `@ApiStatus.Internal` on the action class, which is NOT the
   `<action internal="true"/>` XML attribute the plan and `@McpDescription`
   claim to filter on.** The fixer commit message (`733906d`) frames the
   substitution as "removed in 252 → detect `@ApiStatus.Internal` on the
   action class instead", but these are different concepts:
   `@ApiStatus.Internal` marks API-stability ("don't depend on this from
   another plugin"), while `<action internal="true"/>` is the
   *runtime-visibility* mechanism that the IDE uses to gate the
   "Tools | Internal Actions" submenu and Ctrl+Shift+A noise. The two
   sets barely overlap — most platform internal-mode actions (e.g.
   `Internal.UI.ShowUiInspector`, `DumpInspectionDescriptions`) are
   public Java classes with no `@ApiStatus.Internal`, while many
   `@ApiStatus.Internal`-marked classes are perfectly user-facing.
   Result: `includeInternal=false` (the default) silently leaks every
   internal-mode platform action into responses, and `includeInternal=true`
   doesn't actually surface more of them. The plan's test bullet ("excludes
   a known `<action internal="true">` id, e.g. one starting with
   `Internal.`") would fail today. Two options: (a) read the
   `internal` flag off the registry stub via `ActionStub.isInternal()`
   (the field is preserved on stubs and is the property the platform
   itself consults), or (b) drop the filter for now and document the
   `includeInternal` arg as a no-op. The plan also suggested a third path
   via XML attribute at registry level — also viable.

2. **[HIGH] `ActionInventory.kt:114-141` — the unfiltered walk calls
   `resolveAction` on EVERY candidate, defeating the plan's "only
   resolve the first `limit` ids" optimisation.** The plan explicitly
   states (line 155): *"Unfiltered walk (no query, no plugin filter,
   limit=200) measured target: < 2 s because we only call
   `getActionOrStub` (no class load) for the first 200 ids."* The
   implementation cannot honour that because the loop ALWAYS resolves
   the action before deciding whether to skip it (needed for `isInternal`
   even when `needSubstring` is false). For a 3000-id catalog with
   default `limit=200` and no query, that's 3000 stub lookups (≈3000
   reflective method invocations — see finding 3) plus the substring
   filter check for `needSubstring=false` does nothing useful. Fix: skip
   `resolveAction` entirely when `key.includeInternal=true` AND
   `needSubstring=false` AND `out.size >= key.limit` — at that point we
   only need plugin-map + shortcut data, both of which are id-keyed. Or
   short-circuit even earlier: once `out.size == key.limit` AND
   `needSubstring=false`, every remaining id contributes only to
   `matchedCount`, so `matchedCount += candidates.size - i` and `break`.

3. **[MEDIUM] `ActionInventory.kt:159-163` — `am.javaClass.methods.firstOrNull
   { … }` runs per id (≈3000 times for an unfiltered walk).** The
   reflection scan is identical for every call, but the result isn't
   cached. `java.lang.Class.getMethods()` allocates a fresh `Method[]`
   on each invocation, then the `firstOrNull` scan walks it linearly.
   Hoist the lookup to a `Lazy<Method?>` at the class/companion level
   (`ActionManagerImpl` is a singleton — the class is stable across
   calls). This alone should shave a noticeable chunk off the unfiltered
   walk.

4. **[MEDIUM] `ActionInventory.kt:143` — `total` is wrong when the
   limit is hit but no deadline fires.** `matchedCount` is incremented
   AFTER `out.add` regardless of whether the limit gate fired, so
   `matchedCount >= out.size` and `total = max(matchedCount, out.size)
   = matchedCount`, which IS the true match count — OK. But when the
   deadline fires mid-walk, the contract in the plan and the `ActionInfo`
   kdoc says "total equals `actions.size`" (i.e. we DON'T claim we know
   the true total). The implementation instead returns
   `total=matchedCount` which can exceed `actions.size` if the limit
   was hit BEFORE the deadline. That's actually a fine outcome, but the
   `ActionInfo` kdoc (lines 45-47) explicitly states "we cannot know the
   would-be total — in that case it equals `actions.size`" — the kdoc
   and the implementation disagree. Either fix the code or fix the kdoc.

5. **[LOW] `ActionInventory.kt:69` — TtlCache loader closes over `it`
   from the enclosing `computeIfAbsent` block, which works but is
   fragile.** `TtlCache(ttlMs = CACHE_TTL_MS) { collect(it) }` — the
   inner lambda is `() -> T`, has no `it` of its own, and Kotlin resolves
   `it` to the OUTER `computeIfAbsent` lambda's parameter (the
   `CacheKey`). Anyone refactoring later who renames or destructures
   that outer `it` silently breaks every cache load. Make it explicit:
   `perKeyCache.computeIfAbsent(key) { k -> TtlCache(ttlMs = CACHE_TTL_MS)
   { collect(k) } }`.

6. **[LOW] `ActionInventory.kt:114-141` — `<separator>` / `<group>` /
   action-group ids will surface as `ActionInfo` rows.**
   `getActionIdList("")` includes every registered id including action
   groups (which are `ActionGroup`s, a subclass of `AnAction`) and any
   stub that turns out to wrap a group. The plan's edge case 1 lists
   "groups are out of scope" but the implementation doesn't skip
   `ActionGroup` instances. For users searching for "Editor" this means
   `EditorPopupMenu` (a group) appears alongside `EditorCopy` (an
   action) with no way to tell them apart in the response.
   Cheap fix: `if (anAction is ActionGroup) continue` after `resolveAction`,
   or expose a `kind` field on `ActionInfo`.

### Threading / EDT

7. **[OK] `KeymapManager.activeKeymap.getShortcuts(id)` is
   documented background-safe.** The platform Javadoc on
   `Keymap.getShortcuts` explicitly states "Can be called in background
   thread" (confirmed via web research, see References below), so the
   off-EDT shortcut walk is correct and the plan's open question is
   resolved. No EDT wrap needed.

8. **[INFO] `ActionManager.getAction(id)` may trigger classloading,
   which holds a lock.** This is unavoidable for the substring path —
   `templatePresentation.text` requires the loaded class. The current
   `getActionOrStub` fast-path is the right mitigation. Worth noting:
   per the plan, prefer `getActionOrStub` for *every* path; today the
   code already does (line 156-167). Just keep an eye on classloader
   pressure if a future change loops `getAction` directly.

### Test coverage gaps

9. **[HIGH] No `ActionInventoryPlatformTest` exists.** The plan
   explicitly lists six platform-level assertions (lines 210-223): well-
   known ids (`EditorCopy`, `RunAction`), exact-match-with-truncation,
   empty result, internal-action exclusion, and cache identity. None
   ship. Without them, findings 1 (`isInternal` mis-detection), 2
   (perf), and 6 (groups in results) would have been caught — that's
   exactly the test the platform layer is for. The pure-JVM
   `ActionInventoryTest` only covers `isPrefixQuery` and `CacheKey`
   equality, neither of which is at risk.

10. **[MEDIUM] No regression test for the `total` semantics in
    finding 4.** A two-line platform test asserting
    `list_actions(query=null, limit=10).total == 10 &&
    .actions.size == 10 && .truncated == true` would lock the contract
    down.

### Plan-vs-impl gaps

11. **Plan §"Timeout strategy" says `withTimeoutOrNull(10_000)` ; impl
    uses a manual `System.currentTimeMillis()` deadline.** Both are
    valid 10 s caps, both honour the CLAUDE.md "10 s, no exceptions"
    rule. The deadline approach is actually preferable here because
    the wall-clock check is cheap and `withTimeoutOrNull` would cancel
    mid-`getAction(id)` (interruptible point), risking a partially-
    loaded class. Document the divergence in a one-line comment on
    line 78 rather than silently differing from the plan.

12. **Plan §"category" promises null-by-default, opt-in later; impl
    matches.** `category` is hard-coded null in `toActionInfo`. Fine.

13. **Plan §"reverse plugin map cached per entry"; impl rebuilds it
    per `collect()` call.** Each cache entry rebuilds the map even
    though it's identical across all cache keys (it depends only on
    the loaded plugin set, not on query / pluginId / includeInternal /
    limit). The map walks every loaded plugin's `getPluginActions`,
    which itself does a registry scan. Hoist to a separate
    `TtlCache<Map<String, Pair<String, String?>>>` with the same 60 s
    TTL — would noticeably speed up first-call latency when an agent
    runs many distinct queries in a row.

### Cross-cutting

14. **[LOW] `ActionInfo.iconPath`** is `templatePresentation.icon?.toString()`,
    which produces strings like `IconLoader$CachedImageIcon@1234` for
    procedural icons — not a path. The kdoc on `ActionInfo` (line 23)
    already concedes this ("best-effort: some icons stringify to
    garbage"). Same caveat lives in `ui.list_tool_windows`. Consistent
    with rest of codebase — leave as is.

15. **[LOW] `@McpDescription` parameter doc for `query`** mentions the
    regex `^[A-Za-z0-9_.]+$` verbatim. End users (LLMs) don't need the
    implementation pattern — better as "alphanumeric, dots and
    underscores; no whitespace or wildcards". Trivial cleanup.

## Research notes

- `Keymap.getShortcuts(actionId)` is explicitly background-safe per the
  Javadoc on `intellij-community/platform/platform-api/src/com/intellij/
  openapi/keymap/Keymap.java` ("Can be called in background thread"). No
  `@ApiStatus` markers; long-stable. Fetched via WebFetch.
- `ActionManagerEx.getActionIdList(prefix)` returns an unmodifiable
  collection of every registered action id matching the prefix WITHOUT
  instantiating any `AnAction`. Confirmed in
  `intellij-community/platform/editor-ui-api/src/com/intellij/openapi/
  actionSystem/ActionManager.java`.
- `ActionManager.getActionOrStub(id)` exists in master and returns the
  `ActionStub` rather than triggering full class load when available.
  No `@ApiStatus.Internal` marker visible on the method itself in the
  current source — but the class-level marker on `ActionManager`'s
  constructor governs the whole class. Reflective access (as in our
  code) is the conservative choice but the method has been stable for
  years.
- `AnAction.isInternal()` is genuinely **absent** from the current
  master `AnAction.java`. The fixer's substitution to
  `@ApiStatus.Internal` reflection is the wrong replacement — see
  finding 1. The right replacement is `ActionStub.isInternal()` (the
  stub field carries the XML attribute), or reading the XML directly.
- JetBrains/mcp-server-plugin's old `ListAvailableActionsTool` is the
  known-slow reference path (verified: it does
  `actionIds = actionManager.getActionIdList(""); ids.mapNotNull {
  actionManager.getAction(it) }` — eagerly instantiates every action).
  Our prefix fast-path is the right differentiator, but finding 2
  shows the unfiltered path is closer to the OSS bad-path than the
  plan claims.

## References

- IntelliJ master `Keymap.java`:
  https://github.com/JetBrains/intellij-community/blob/master/platform/platform-api/src/com/intellij/openapi/keymap/Keymap.java
- IntelliJ master `ActionManager.java`:
  https://github.com/JetBrains/intellij-community/blob/master/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/ActionManager.java
- IntelliJ master `AnAction.java` (no `isInternal()`):
  https://github.com/JetBrains/intellij-community/blob/master/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/AnAction.java
- IntelliJ Internal Mode docs:
  https://plugins.jetbrains.com/docs/intellij/enabling-internal.html
- IntelliJ Internal Actions Menu:
  https://plugins.jetbrains.com/docs/intellij/internal-actions-intro.html
- Plan: `/home/user/ide-introspector-plugin/docs/plans/arch-list-actions.md`
- Fixer commit `733906d` (notes the `isInternal()` substitution).
