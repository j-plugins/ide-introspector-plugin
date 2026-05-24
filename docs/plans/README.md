# Integration Plan — Phase 3 MCP Tools

Implementation plans for ~24 new MCP tools across 6 existing groups and 3 new groups,
derived from the May 2026 post-JetBrains-overlap research. Every plan is a separate
file in this directory; this README is the index, the shared template, and the
cross-cutting conventions.

## Strategic context

The IDE Introspector currently exposes 21 tools across six groups (`ui.*`, `screenshot.*`,
`arch.*`, `psi.*`, `code.*`, `exec.*`). The shipped JetBrains MCP server in
IntelliJ 2025.2+ covers ~57 built-in tools across 17 categories — file ops, search,
build, run, debug, VCS, refactor, terminal, etc.

These plans deliberately target the **gap** JetBrains does NOT cover:

1. **Plugin-architecture introspection** — services, listeners, EP schema, devkit checks.
2. **Swing/UI semantic listing** — tool windows, dialogs, action invocation on a component.
3. **Advanced PSI navigation** — symbol_at, outline, type/method hierarchy, goto_implementation.
4. **Screenshots** — highlight overlay, pixel diff.
5. **Observability** — idea.log tail, indexing/memory health.
6. **Editor mutation** — `editor.set_caret`, `editor.get_state` (NOT `editor.open_file` —
   JetBrains already has `open_file_in_editor`).
7. **Code catalog & exec extras** — by-module/package class iteration, compile-only check.

Out of scope (JetBrains already covers): `search.*`, full `vcs.*`, full `project.*`,
generic `inspect.get_problems`, `actions.invoke`, refactorings.

## Plan inventory

| # | Pri | Tool(s) | Group | File | Effort |
|---|-----|---------|-------|------|--------|
| 1 | P0 | `arch.list_services` | `arch.*` | [arch-list-services.md](arch-list-services.md) | 1d |
| 2 | P0 | `arch.list_listeners` | `arch.*` | [arch-list-listeners.md](arch-list-listeners.md) | 1d |
| 3 | P0 | `arch.get_extension_point_details` | `arch.*` | [arch-get-extension-point-details.md](arch-get-extension-point-details.md) | 1d |
| 4 | P0 | `arch.list_actions` | `arch.*` | [arch-list-actions.md](arch-list-actions.md) | 1.5d |
| 5 | P0 | `arch.check_lock_requirements` + `arch.check_threading_requirements` | `arch.*` | [arch-devkit-mirror.md](arch-devkit-mirror.md) | 1.5d |
| 6 | P0 | `ui.list_tool_windows` + `ui.list_dialogs` | `ui.*` | [ui-semantic-listing.md](ui-semantic-listing.md) | 1d |
| 7 | P0 | `ui.invoke_action_on` | `ui.*` (opt-in) | [ui-invoke-action-on.md](ui-invoke-action-on.md) | 1.5d |
| 8 | P0 | `psi.symbol_at` + `psi.get_outline` | `psi.*` | [psi-symbol-and-outline.md](psi-symbol-and-outline.md) | 1.5d |
| 9 | P0 | `psi.type_hierarchy` + `psi.goto_implementation` | `psi.*` | [psi-hierarchy-navigation.md](psi-hierarchy-navigation.md) | 1.5d |
| 10 | P1 | `screenshot.highlight` + `screenshot.diff` | `screenshot.*` | [screenshot-extensions.md](screenshot-extensions.md) | 1d |
| 11 | P1 | `editor.set_caret` + `editor.get_state` | `editor.*` (new) | [editor-group.md](editor-group.md) | 1d |
| 12 | P1 | `log.tail` + `log.errors_since` | `log.*` (new) | [log-group.md](log-group.md) | 1d |
| 13 | P1 | `health.indexing_status` + `health.memory` | `health.*` (new) | [health-group.md](health-group.md) | 0.5d |
| 14 | P2 | `code.list_classes_in_module` + `code.list_classes_in_package` | `code.*` | [code-class-catalog.md](code-class-catalog.md) | 1d |
| 15 | P2 | `exec.compile_check` | `exec.*` | [exec-compile-check.md](exec-compile-check.md) | 0.5d |

Total: **~24 new tools across 15 plans, ~15 implementation-days**. After Phase 3
the plugin would expose ~45 tools across 9 groups.

## Implementation order

Suggested wave-based ordering — earlier waves enable later ones and deliver
quick wins first.

**Wave A — leverage existing `arch.*` infrastructure (≈4 d):**
1. `arch.get_extension_point_details` (extends EP enumeration we already have)
2. `arch.list_services`
3. `arch.list_listeners`
4. `arch.list_actions`

**Wave B — extend existing groups, no new META-INF wiring (≈5 d):**
5. `psi.symbol_at` + `psi.get_outline`
6. `psi.type_hierarchy` + `psi.goto_implementation`
7. `screenshot.highlight` + `screenshot.diff`
8. `ui.list_tool_windows` + `ui.list_dialogs`
9. `arch.check_lock_requirements` + `arch.check_threading_requirements`

**Wave C — new tool groups (≈3 d) — each adds a new `McpToolset`:**
10. `editor.*` group (set_caret + get_state)
11. `log.*` group (tail + errors_since)
12. `health.*` group (indexing_status + memory)

**Wave D — high-risk / opt-in (≈3 d):**
13. `ui.invoke_action_on` — opt-in confirmation like `exec.*`
14. `exec.compile_check` — compile only, no run, default-on (no confirm)
15. `code.list_classes_in_module` + `code.list_classes_in_package` — beware of huge JDK catalogs

---

## Per-feature plan template

Every per-feature plan file under this directory MUST follow this structure.
Keep total length 80–250 lines per file; longer than that signals the feature
should be split.

```
# <Tool name(s) or feature title>

## Purpose & motivation
- One paragraph: WHAT this tool does, WHY it's needed, what gap it fills
  vs. existing IDE Introspector tools AND vs. JetBrains' built-in MCP server.
- One sentence: success criterion (what an agent can do with it that it couldn't before).

## Tool specification
For each tool the plan introduces:

### `group.tool_name`
**Signature:**
```kotlin
@McpTool(name = "group.tool_name")
@McpDescription("…")
suspend fun toolName(
    @McpDescription("…") param1: Type,
    @McpDescription("…") param2: Type = default,
): ResponseModel
```

**`@McpDescription` draft** (full text, structured per CLAUDE.md "Tool descriptions"
section: What it does / Use this when / Do NOT use this when / Returns / Examples).

**Args** — every parameter, type, default, validation, units.

**Response model** — full `@Serializable` data class(es) outline.

## IntelliJ APIs used
- Class / method / EP names with `org.intellij…` FQN.
- Stability notes (is this an internal API? @ApiStatus.Experimental?).
- Cite the source: official platform docs URL, or the source file in IntelliJ Community.

## Threading & EDT model
- Does this need EDT? PSI ReadAction? Both?
- If EDT-bouncing: `onEdtBlocking { … }` with `ModalityState.any()`.
- If PSI: wrap in `ReadAction.compute<T>` or `runReadAction`.
- Cache: TtlCache (already used in `core/internal/ExtensionMetadata.kt`)? Per-call?

## Timeout strategy
- Hard 10 s cap per project rule (CLAUDE.md).
- Where this tool could exceed 10 s, what the mitigation is (narrow scope,
  default limit, stream/page).

## Edge cases
Enumerate at least 5 (more for complex tools):
- Empty/null project
- Project in dumb mode / indexing
- API method that throws on some bundled plugins (cf. the
  `ep.extensionList.size` pitfall in CLAUDE.md)
- Disabled plugins
- Inner classes / generics / nullables
- Multi-PSI files / injections
- etc.

## Files to create/modify

| Path | Op | What |
|------|----|------|
| `src/main/kotlin/com/github/xepozz/ide/introspector/tools/XxxToolset.kt` | Edit | Add `@McpTool` method |
| `src/main/kotlin/com/github/xepozz/ide/introspector/core/XxxInspector.kt` | Create | Logic |
| `src/main/kotlin/com/github/xepozz/ide/introspector/model/XxxInfo.kt` | Edit/Create | `@Serializable` response |
| `src/main/kotlin/com/github/xepozz/ide/introspector/model/args/XxxArgs.kt` | Edit | `@Serializable` args |
| `src/main/resources/META-INF/xxx.xml` | Create | Conditional load (only if new group) |
| `src/test/kotlin/.../XxxInspectorTest.kt` | Create | Unit |
| `src/test/kotlin/.../platform/XxxInspectorPlatformTest.kt` | Create | Platform |

For NEW tool groups, also note:
- New `<depends optional="true">` line in `mcp-integration.xml` (or the right shim)
- New `<mcpServer.mcpToolset>` registration

## Test plan
- Unit tests: pure-logic things (parsers, formatters, filters) — pattern in
  `src/test/kotlin/.../core/*Test.kt`.
- Platform tests: anything that touches IntelliJ Platform classes — pattern in
  `src/test/kotlin/.../core/platform/*PlatformTest.kt` (extends
  `BasePlatformTestCase`).
- Specific scenarios to test (at least 3).

## Estimated effort
- Hours/days breakdown.

## Open questions / risks
- API stability, edge cases, possible follow-ups.

## References
- Existing code: link to similar `arch.*` / `psi.*` toolset method already in repo.
- IntelliJ source: link to relevant platform class on
  `https://github.com/JetBrains/intellij-community` if cited.
- JetBrains MCP equivalent (if any) and how this differs.
```

---

## Cross-cutting conventions

These apply to EVERY plan and EVERY implementation. Restate or reference them
in your plan — do not omit.

### Hard 10 s timeout rule

Everything async/blocking caps at 10 s (CLAUDE.md "Timeouts"). If you can't fit a
realistic worst case under 10 s, **make the tool cheaper** (smaller scope, default
limits, paging) instead of raising the cap. Common knobs already in use:

- `EdtHelpers.DEFAULT_EDT_TIMEOUT_MS = 10_000L`
- `ExecSettings.maxTimeoutMs = 10_000L`
- All `@McpTool` methods take a `limit: Int = <reasonable default>` argument when
  the result set can be unbounded.

### Conditional loading

- Tools that should ALWAYS be available when the MCP server is present →
  `src/main/resources/META-INF/mcp-integration.xml`.
- Tools that need `com.intellij.modules.java` → `META-INF/java-introspect.xml`.
- Tools that need `org.jetbrains.kotlin` (currently only `exec.*`) →
  `META-INF/kotlin-exec.xml`.
- **New** optional dependency? Add a new `<depends optional="true" config-file="…"/>`
  line in `mcp-integration.xml` and a new shim XML.

### Doc generation

`docs/MCP_TOOLS.md` is regenerated by `doc-processor/` from
`@McpTool` + `@McpDescription` annotations on every `./gradlew compileKotlin`. Do
NOT edit `docs/MCP_TOOLS.md` by hand — write the annotations and rebuild.

### Threading

- MCP `suspend fun` methods run on a background ktor coroutine, NOT EDT.
- Swing access ⇒ `onEdtBlocking { … }` (uses `ModalityState.any()`).
- PSI / VFS reads ⇒ `ReadAction.compute<T> { … }` or `runReadAction { … }`.
- `ExtensionPoint` enumeration is thread-safe — no EDT needed for `arch.*` walks.

### Naming / style

- Tool name = `group.snake_case_verb` (e.g. `arch.list_services`).
- One `McpToolset` class per group; new groups get their own class.
- `@Serializable` response types in `model/`, args in `model/args/`.
- `core/<Topic>Inspector.kt` for the headless logic class (so it can be unit-tested
  independently of the toolset wrapper).
- Trim-margin Kotlin strings (`""" |line… """`) for multi-line `@McpDescription`s —
  the reflection bridge calls `trimMargin` automatically.

### Testing layout

- Unit tests under `src/test/kotlin/.../core/` — pure JVM, no IntelliJ runtime.
- Platform tests under `src/test/kotlin/.../core/platform/` — extend
  `BasePlatformTestCase`; load fixture files via test data path.
- Toolset wrappers (the `@McpTool` methods themselves) are usually thin enough
  not to need their own test class — the Inspector tests cover the logic.

### Caching

- `core/internal/TtlCache.kt` is the canonical cache wrapper (see
  `ExtensionMetadata` for an example consumer).
- Default TTL for "rarely changes" data (plugin inventory, EP list) — 60 s.
- Project-scoped caches should key on `Project` or `Project.locationHash`, never
  globally.

### Opt-in tools (security-sensitive)

Anything that mutates state, runs code, or invokes actions follows the
`exec.*` pattern:

1. Off by default in settings (`PersistentStateComponent`).
2. Per-call confirmation dialog (with session-only "don't ask again").
3. Pre-execution safety check (text blacklist or AST analysis).
4. Audit log to `idea.log` (category `ide-introspector-audit`).
5. Hard timeout cap.

Currently only `exec.execute_kotlin_in_ide` follows this. `ui.invoke_action_on`
(Phase 3) will too.

---

## How to read this directory

1. Start here for the big picture and the template.
2. Pick a plan file matching the work you're about to do.
3. The plan tells you which files to touch, in what order, with what tests.
4. After implementation, the relevant `@McpDescription` annotation regenerates
   `docs/MCP_TOOLS.md` — no manual doc-keeping.
