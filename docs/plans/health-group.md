# `health.*` group — indexing & memory observability

## Purpose & motivation

A new `health.*` MCP tool group that gives agents a cheap, side-effect-free way to
ask **"is the IDE healthy enough to call X?"** before launching a long sequence of
PSI / index-dependent calls (`arch.*`, `psi.*`, `code.*`). Agents currently have
no way to detect that the IDE is mid-indexing — they fire `psi.find_usages`,
hit `IndexNotReadyException` or a multi-minute stall, and surface a confusing
error to the user. The two tools also give plugin developers a "what is the IDE
doing right now?" snapshot without screen-sharing. JetBrains' built-in MCP server
exposes **neither** indexing state nor JVM memory.

**Success criterion:** an agent can run `health.indexing_status` and skip / wait
before issuing index-bound PSI calls, and can run `health.memory` to detect a
near-OOM IDE before triggering a heavy `screenshot.*` capture.

## Tool specifications

### `health.indexing_status`

**Signature:**
```kotlin
@McpTool(name = "health.indexing_status")
@McpDescription(""" |… see below … """)
suspend fun indexingStatus(
    @McpDescription("Optional project locationHash filter; omit to report all open projects.")
    projectHash: String? = null,
): IndexingStatus
```

**`@McpDescription` draft** (verbatim — trim-margin):

```
|Reports whether the IDE is currently indexing files or in "dumb mode", per open project.
|
|Use this when:
| - About to call any `psi.*`, `arch.*`, or `code.*` tool and you want to avoid
|   `IndexNotReadyException` / multi-minute stalls.
| - Debugging why a previous index-dependent call returned an empty / partial result.
| - Polling for "indexing finished" before kicking off a batch of PSI queries.
|
|Do NOT use this when:
| - You just want JVM memory — use `health.memory`.
| - You need per-file index state — this is project-level only.
| - You want to *trigger* indexing — this tool is read-only.
|
|Returns: {
|  dumbMode: Boolean,            // true if ANY open project is in dumb mode
|  isStartupComplete: Boolean,   // true once every open project finished post-startup
|  currentTask: String?,         // human-readable current dumb-mode task, if any
|  queuedTasks: Int,             // dumb-mode tasks queued behind the current one
|  projectsIndexing: [{ projectName, projectHash, dumbModeActive,
|                       indexingActive, scanningActive, currentTask? }]
|}
|
|Examples:
|  health.indexing_status                                # all open projects
|  health.indexing_status projectHash="a1b2c3"           # one project
```

**Args:** `projectHash: String? = null` — IntelliJ `Project.locationHash`; if non-null,
the response's `projectsIndexing` is filtered to that single project (empty if no match).

**Response model** (in `model/HealthInfo.kt`):

```kotlin
@Serializable data class IndexingStatus(
    val dumbMode: Boolean,
    val isStartupComplete: Boolean,
    val currentTask: String? = null,
    val queuedTasks: Int = 0,
    val projectsIndexing: List<ProjectIndexingState> = emptyList(),
)

@Serializable data class ProjectIndexingState(
    val projectName: String,
    val projectHash: String,
    val dumbModeActive: Boolean,
    val indexingActive: Boolean,
    val scanningActive: Boolean,
    val currentTask: String? = null,
)
```

### `health.memory`

**Signature:**
```kotlin
@McpTool(name = "health.memory")
@McpDescription(""" |… see below … """)
suspend fun memory(
    @McpDescription("If true, call System.gc() once before sampling. GC is a HINT — heap may not shrink. Default false.")
    gcBeforeRead: Boolean = false,
): MemorySnapshot
```

**`@McpDescription` draft** (verbatim):

```
|Reports JVM memory + a few IDE-specific counters from `java.lang.management` MXBeans.
|
|Use this when:
| - You suspect the IDE is near-OOM (slow response, freezes, GC thrashing).
| - You want a baseline before a heavy operation (`screenshot.capture`, large
|   `psi.get_tree`, `exec.execute_kotlin_in_ide`).
| - Diagnosing a memory-leak report from a user.
|
|Do NOT use this when:
| - You want per-object retention — use a real profiler (YourKit / JFR / VisualVM).
| - You need indexing state — use `health.indexing_status`.
| - `gcBeforeRead=true` looks like a fix — `System.gc()` is a HINT to the JVM
|   and may do nothing. Never rely on it to "free" memory.
|
|Returns: {
|  heap:      { used, max, committed, freeBytes },           // bytes
|  metaspace: { used, max },                                 // bytes; max may be -1 (unbounded)
|  nonHeap:   { used, max },                                 // bytes
|  threadCount: Int,
|  classCount:  Int,
|  gcs: [{ name, collectionCount, collectionTimeMs }],       // per-GC counters since JVM start
|  uptime:          Long,                                    // ms since JVM start
|  uptimeFormatted: String                                   // e.g. "1h 12m 03s"
|}
|
|Examples:
|  health.memory                            # cheap snapshot
|  health.memory gcBeforeRead=true          # rare; intrusive
```

**Args:** `gcBeforeRead: Boolean = false`. Default false because `System.gc()` is a
full stop-the-world hint and is intrusive on a live IDE.

**Response model** (in `model/HealthInfo.kt`):

```kotlin
@Serializable data class MemorySnapshot(
    val heap: MemoryUsageBlock,
    val metaspace: MemoryUsageBlock,
    val nonHeap: MemoryUsageBlock,
    val threadCount: Int,
    val classCount: Int,
    val gcs: List<GcStat>,
    val uptime: Long,
    val uptimeFormatted: String,
)

@Serializable data class MemoryUsageBlock(
    val used: Long, val max: Long, val committed: Long, val freeBytes: Long,
)

@Serializable data class GcStat(
    val name: String, val collectionCount: Long, val collectionTimeMs: Long,
)
```

## IntelliJ / JDK APIs used

Indexing:
- `com.intellij.openapi.project.DumbService.getInstance(project).isDumb` — volatile read.
- `DumbService#getCurrentTask()?.indicator?.text` — current dumb-mode task name (may be null).
- `com.intellij.openapi.project.DumbServiceImpl` (internal) for `queuedTasksCount` if
  reachable via reflection — otherwise omit and set `queuedTasks = 0`.
- `com.intellij.util.indexing.UnindexedFilesScannerExecutor.getInstance(project).isRunning`
  — `@ApiStatus.Internal`; fall back to `false` via reflection-guarded call.
- `com.intellij.openapi.startup.StartupManager.getInstance(project).postStartupActivityPassed()`.
- `com.intellij.openapi.project.ProjectManager.getInstance().openProjects`.
- `Project.locationHash` for stable per-project IDs.

Memory:
- `java.lang.management.ManagementFactory.getMemoryMXBean()` — heap + non-heap usage.
- `ManagementFactory.getMemoryPoolMXBeans()` — pick the pool whose name contains
  `"Metaspace"` (HotSpot / JBR). May not exist on every JVM.
- `ManagementFactory.getGarbageCollectorMXBeans()` — per-GC counters.
- `ManagementFactory.getThreadMXBean().threadCount`.
- `ManagementFactory.getClassLoadingMXBean().loadedClassCount`.
- `ManagementFactory.getRuntimeMXBean().uptime`.

Stability: `DumbService`, `StartupManager`, `ProjectManager` are stable IntelliJ
public API. `UnindexedFilesScannerExecutor` and `DumbServiceImpl` internals are
`@ApiStatus.Internal` — wrap reflection calls in try/catch and degrade
gracefully (set the missing boolean to `false`, the missing counter to `0`).
All `ManagementFactory` MXBeans are part of the JDK platform API.

## Threading & EDT model

- Both tools are **non-EDT-safe to read**. No `onEdtBlocking` required.
- `DumbService.isDumb` is a volatile read, safe from any thread (documented).
- `ManagementMXBean` instances are thread-safe per the JMX spec.
- No ReadAction / PSI access — neither tool touches PSI.
- No caching. Both tools answer "right now" questions; a TTL would defeat the
  purpose (`indexing_status` is polled exactly because the agent expects it to
  change).

## Timeout strategy

- Both calls return in <10 ms typical. Well inside the 10 s project cap.
- `gcBeforeRead=true` could in theory extend a `memory` call to a few hundred ms
  on a multi-GB heap, but still well under 10 s. No explicit timeout wrapper
  needed.

## Edge cases

- **No project open** → `projectsIndexing: []`, `dumbMode: false`,
  `isStartupComplete: true` (no project means nothing to start).
- **Multiple projects indexing simultaneously** → list every project; top-level
  `dumbMode` is OR-of-per-project; top-level `currentTask` is from the first
  project whose `dumbModeActive=true` (deterministic by `openProjects` order).
- **`projectHash` filter matches nothing** → top-level booleans still reflect
  global state, but `projectsIndexing` is empty. Document in description.
- **`UnindexedFilesScannerExecutor` reflection fails** on an IDE build where the
  class moved or was renamed → catch, set `scanningActive = false`, log once.
- **`System.gc()` is a no-op** in some JVMs (`-XX:+DisableExplicitGC`) → memory
  numbers won't change. Already called out in `@McpDescription`.
- **Metaspace pool missing** on an exotic JVM (J9, Zing variants) → return
  `MemoryUsageBlock(0, -1, 0, 0)` rather than throw.
- **GC names with unusual characters** (e.g. `"G1 Young Generation"`) → pass
  through unchanged; consumers only read.
- **Negative `max` values** are valid per JMX spec ("unbounded" pools); pass
  through unchanged and document.
- **Headless / unit-test runtime** → `ProjectManager.openProjects` is empty;
  tool degrades to memory-only useful output. No special path needed.

## Files to create / modify

| Path | Op | What |
|------|----|------|
| `src/main/kotlin/com/github/xepozz/ide/introspector/tools/HealthToolset.kt` | Create | `McpToolset` with two `suspend` methods, delegating to `HealthReporter`. |
| `src/main/kotlin/com/github/xepozz/ide/introspector/core/HealthReporter.kt` | Create | Pure logic. Memory path uses only `java.lang.management`. Indexing path uses `DumbService` / `StartupManager` / `ProjectManager`, plus a reflection-guarded helper for `UnindexedFilesScannerExecutor`. |
| `src/main/kotlin/com/github/xepozz/ide/introspector/model/HealthInfo.kt` | Create | `IndexingStatus`, `ProjectIndexingState`, `MemorySnapshot`, `MemoryUsageBlock`, `GcStat`. All `@Serializable`. |
| `src/main/resources/META-INF/mcp-integration.xml` | Edit | Add `<mcpServer.mcpToolset implementation="…HealthToolset"/>`. No new optional dependency — both tools work in every IDE that hosts `com.intellij.mcpServer`. |
| `src/test/kotlin/.../core/HealthReporterMemoryTest.kt` | Create | Pure-JVM unit test. |
| `src/test/kotlin/.../core/platform/HealthReporterIndexingPlatformTest.kt` | Create | `BasePlatformTestCase` — verifies `isDumb=false` and `isStartupComplete=true` against the test fixture. |

No new `<depends optional="true">` line — both tools rely only on the IntelliJ
core platform.

## Test plan

Unit (`HealthReporterMemoryTest`, pure JVM, no IntelliJ runtime):
1. `memory()` returns a `MemorySnapshot` with `heap.used > 0` and `heap.max > heap.used`.
2. `gcs` is non-empty (every JVM has at least one collector) and every `GcStat.name`
   is non-blank.
3. `uptimeFormatted` matches `"Xh Ym Zs"` / `"Ym Zs"` / `"Zs"` patterns.
4. `gcBeforeRead=true` returns successfully (no exception when GC is a no-op).
5. Metaspace block tolerates missing pool — synthetic test substitutes an empty
   pool list and verifies graceful zero/-1 fallback.

Platform (`HealthReporterIndexingPlatformTest`, extends `BasePlatformTestCase`):
1. With the fixture project loaded, `indexingStatus()` returns `dumbMode=false`,
   `isStartupComplete=true`, `projectsIndexing.size == 1`.
2. `projectHash` filter: passing the fixture project's `locationHash` returns it;
   passing `"nonsense"` returns an empty list.
3. Reflection-guarded scanner check does not throw even if the API is missing
   in the test SDK.

## Estimated effort

~0.5 day total.
- `HealthReporter` memory path + `MemorySnapshot` model: 1 h (mostly typing).
- `HealthReporter` indexing path + reflection guards: 1.5 h.
- `HealthToolset` wiring + `mcp-integration.xml` edit: 30 min.
- Tests (unit + platform): 1 h.
- `@McpDescription` polish + doc regeneration: 30 min.

## Open questions / risks

- **Cache directory size in `indexing_status`?** Could include
  `PathManager.getIndexRoot()` byte size — interesting for "why is my IDE slow?"
  diagnostics. Rejected for v1: a `Files.walk` over the index dir can take
  seconds on a large project and violates the "trivial, <10 ms" property of this
  tool. Revisit as a separate `health.disk_usage` tool if asked for.
- **Delta tracking on `health.memory`?** Stash the previous reading in a
  light-weight `Service` and return `deltaSinceLastCall` fields. Rejected for
  v1: agents can compute deltas themselves between two calls, and a stateful
  service complicates the otherwise pure tool. Revisit only if a real consumer
  asks.
- **Combine into a single `health.status` tool?** Considered and rejected. The
  two have very different call cadences — agents poll `indexing_status` until
  indexing finishes, then call `memory` once before a heavy op. Forcing both in
  one response would either over-fetch (every memory call samples indexing) or
  add a "section" arg that defeats the simplicity goal. Keep them separate.
- **`UnindexedFilesScannerExecutor` is `@ApiStatus.Internal`** — could move or
  be removed in a future IntelliJ build. Mitigation: reflection-guarded call,
  graceful degradation to `scanningActive = false`. CI Plugin Verifier may warn;
  if so, treat it like the other accepted warnings in CLAUDE.md.
- **`DumbServiceImpl.queuedTasksCount`** isn't a stable public field; the
  reflection may return null on some IDE versions. Fallback: `queuedTasks = 0`.

## References

- Existing toolset pattern: `tools/ArchitectureToolset.kt` — same constructor-less
  `McpToolset` shape, same `@McpTool` / `@McpDescription` annotations.
- Existing reporter-style class: `core/PluginInventory.kt` — pure logic, used
  both by an `McpToolset` and by the Platform Explorer tool window. `HealthReporter`
  follows the same shape; the tool window may surface it in a future iteration.
- IntelliJ source: `platform/core-api/src/com/intellij/openapi/project/DumbService.kt`
  on `github.com/JetBrains/intellij-community`.
- JetBrains MCP equivalent: **none**. Their server has no indexing-state or
  JVM-memory tool; this group is pure-additive and the primary reason agents
  will adopt it.
