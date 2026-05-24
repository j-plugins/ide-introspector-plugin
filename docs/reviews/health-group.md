# Review: health.indexing_status + health.memory (`health.*` group)

Branch: `claude/project-features-analysis-odEwP` @ HEAD (sole feature
commit `0c0bdb1`). Scope: `tools/HealthToolset.kt`,
`core/HealthReporter.kt`, `model/HealthInfo.kt`, the new
`<mcpServer.mcpToolset>` line in `META-INF/mcp-integration.xml`, two
test files (`HealthReporterMemoryTest.kt`,
`HealthReporterIndexingPlatformTest.kt`).

## Verdict
Needs changes before merge. The memory path is solid (clean MXBean
wiring, exhaustive unit coverage including injected fakes, JBR-friendly
`Metaspace`-substring filter that doesn't collide with
`Compressed Class Space`), model + KSP doc-gen land correctly, and
`mcp-integration.xml` registers in the always-on section as the plan
requires — but the *indexing* half is structurally broken: the
`UnindexedFilesScannerExecutor` class lives at
`com.intellij.openapi.project.UnindexedFilesScannerExecutor` (verified
on `master`), and NONE of the three FQNs `scannerRunning()` tries to
load match, so `scanningActive` is always `false`; on top of that,
`DumbServiceImpl` exposes neither `getCurrentTask` nor
`queuedTasksCount` on master, so `currentTask` is always `null` and
`queuedTasks` is always `0`. Three of the six fields in
`ProjectIndexingState` / `IndexingStatus` are dead. The agent learns
nothing about indexing progress beyond `dumbModeActive`.

## Top finding
**Finding 1 (HIGH)** — `scannerRunning()` looks up
`UnindexedFilesScannerExecutor` at three FQNs under
`com.intellij.util.indexing.*`, but the interface lives at
`com.intellij.openapi.project.UnindexedFilesScannerExecutor` (verified
on master). `loadClass(...)` returns `null`, the method short-circuits
to `false`, and `scanningActive` is ALWAYS `false` regardless of
actual scanner state. The feature the plan promised
("`scanningActive` true if `UnindexedFilesScannerExecutor` is
scanning") is not delivered.

## Summary
- Three source files (`HealthReporter.kt` 290 LOC, `HealthToolset.kt`
  93 LOC, `HealthInfo.kt` 77 LOC), one `mcp-integration.xml` line,
  two test files (258 LOC pure-JVM + 151 LOC platform).
- Both `@McpDescription` strings match the plan verbatim, 5-section
  trim-margin format. `mcp-integration.xml:10` correctly placed in the
  always-on extension block (no `<depends>` change). `docs/MCP_TOOLS.md`
  includes both tools at lines 1078 / 1118 — KSP doc-gen works.
- Hard rules: no EDT, no `ReadAction`, no `withTimeoutOrNull` (well
  under 10 s — JMX reads + a `System.gc()` hint cap at ~100 ms even on
  multi-GB heaps). `kotlinx-serialization-json` policy untouched.
- Memory path: `MemoryMXBean` + `List<MemoryPoolMXBean>` are
  constructor-injected — metaspace fallback test passes an empty list
  (`HealthReporterMemoryTest.kt:117-125`) and a `FakeMetaspacePool`
  (`:128-143`). `formatUptime` exhaustively covered, including day-form
  (`:194-199`) and negative-clamp (`:202-205`).

## Findings

### 1. [HIGH] `scannerRunning()` FQNs don't match where `UnindexedFilesScannerExecutor` actually lives — `scanningActive` permanently `false`

`HealthReporter.kt:154-159`:
```kotlin
val klass = loadClass(
    "com.intellij.util.indexing.UnindexedFilesScannerExecutor",
    "com.intellij.util.indexing.dependencies.UnindexedFilesScannerExecutor",
    "com.intellij.util.indexing.diagnostic.dependencies.UnindexedFilesScannerExecutor",
) ?: return false
```

Cross-checked on `master` of `JetBrains/intellij-community`: the
interface lives at
`platform/platform-impl/src/com/intellij/openapi/project/UnindexedFilesScannerExecutor.kt`
— package `com.intellij.openapi.project`, NOT `com.intellij.util.indexing`.
The impl (`UnindexedFilesScannerExecutorImpl`) is at
`platform/lang-impl/src/com/intellij/util/indexing/` but you need the
interface for the static `getInstance(Project)`. Every JetBrains-
internal caller does
`import com.intellij.openapi.project.UnindexedFilesScannerExecutor`
(`AppIdleMemoryCleaner.kt`, `DumbServiceScanningListener.kt`,
`SmartModeScheduler.kt`, `ProjectSyncTask.kt`).

Concrete impact: every `health.indexing_status` returns
`scanningActive: false` in every IDE build. An agent polling while the
project is scanning (no dumb mode yet but disk-IO heavy) gets
`dumbMode=false, scanningActive=false`, concludes ready, fires
`psi.find_usages`, hits a freeze.

Fix: add `"com.intellij.openapi.project.UnindexedFilesScannerExecutor"`
as the **first** entry of `loadClass(...)`. Add a platform test that
asserts `Class.forName("…openapi.project.UnindexedFilesScannerExecutor")`
succeeds on the test SDK. The existing
`isRunning → .getValue()` fallback for `StateFlow<Boolean>` is correct
and stays — bug is solely the FQN list.

### 2. [HIGH] `currentTask` permanently `null` and `queuedTasks` permanently `0` — neither field reachable on stock `DumbServiceImpl`

`HealthReporter.kt:109-129` reflects
`DumbService.getCurrentTask()?.indicator?.text`; `:131-147` reflects
`DumbServiceImpl.getQueuedTasksCount` / bare `queuedTasksCount` field.

Verified on `master`: neither `DumbService.kt` nor `DumbServiceImpl.kt`
exposes either name. Public `DumbService` has only `isDumb`,
`queueTask`, `cancelTask`, experimental `state: StateFlow<DumbState>`
(carrying just `isDumb` + `modificationCounter`). Private internal
fields are `taskQueue: DumbServiceMergingTaskQueue` and
`dumbTaskLaunchers: MutableList<DumbTaskLauncher>` — neither readable
by the names the reflection probes use. Every `runCatching.getOrElse`
chain silently returns null/0.

So `IndexingStatus.currentTask` is always `null`,
`IndexingStatus.queuedTasks` is always `0`, per-project `currentTask`
is always `null`. The plan's "human-readable current dumb-mode task"
promise is unfulfilled.

Fix options:
1. **Drop the dead fields** — remove `currentTask` (top + per-project)
   and `queuedTasks` from the model; update `@McpDescription`.
2. **Surface the real signal** — `getDeclaredField("taskQueue")` on
   the private `DumbServiceMergingTaskQueue`, read `.size`. Document
   the private-field dependency.
3. **Listener-driven** — use `DumbService.DUMB_MODE` topic to maintain
   a small `Service`-scoped counter outside the reflection path.

Either way, the silent `LOG.debug(... reflection failed)` swallow is
invisible in production. Bump to one-time `LOG.warn` (with an
`AtomicBoolean` latch) so the failure surfaces in `idea.log`.

### 3. [MED] `DumbService.isDumb` from a background coroutine — kdoc claim contradicts JetBrains guidance

`HealthReporter.kt:22`:
```kotlin
 *   - `DumbService.isDumb` (documented volatile-read; thread-safe).
```

`DumbService.kt` on master kdoc actually says: *"To avoid race
conditions, use it only in EDT thread or inside read-action."* Not
advertised as a volatile read. The plugin reads `isDumb` directly from
the ktor background coroutine — no EDT bounce, no read action.

Pragmatically this is fine for a snapshot health probe (the value can
change in the next call anyway, and wrapping in a read-action risks
deadlock with the write-action that *changes* dumb mode). But the
kdoc is false and will mislead the next maintainer who copies the
"thread-safe" pattern into a `psi.*` / `code.*` call.

Fix: rewrite the kdoc to:
```kotlin
 *   - `DumbService.isDumb` — racy by design. JetBrains kdoc says to
 *      wrap in a read-action for correctness; we deliberately don't
 *      because a health snapshot is intrinsically a point-in-time
 *      sample, and a read-action could deadlock with the write-action
 *      that *changes* dumb mode.
```

### 4. [MED] `loadClass()` uses bare `Class.forName(n)` — classloader-non-deterministic

`HealthReporter.kt:187-196` uses `Class.forName(n)` without a
classloader. Works at runtime because the plugin `PluginClassLoader`
delegates to the platform parent. But it triggers static init on the
loaded class and varies between unit-test / sandbox contexts. Defensive
fix: `Class.forName(n, /* initialize = */ false, HealthReporter::class.java.classLoader)`.

### 5. [MED] Redundant `.let` after vacuous `.all { … }`

`HealthReporter.kt:57-59`:
```kotlin
val allStartupComplete = perProject.all { startupComplete(projectByHash(projects, it.projectHash)) }
    .let { if (perProject.isEmpty()) true else it }
```
`Iterable.all` on empty returns `true` by definition. The `.let { … }`
is a no-op; the comment is misleading. Drop the `.let`; move the
"vacuous true" comment to a one-liner above `.all`.

### 6. [MED] `projectsIndexing` filter still runs `snapshotProject` over every project

`HealthReporter.kt:55-68`: `snapshotProject` walks EVERY open project
even when `projectHashFilter` selects one. Today invisible (Findings
1 & 2 short-circuit the heavy paths), but once those are fixed each
`service<UnindexedFilesScannerExecutor>()` lookup is a real container
hit. Three open projects + filter = 3× the work.

Fix: filter `projects` before mapping; compute top-level booleans from
the *un-filtered* `projects` array (per the spec — "top-level still
reflects global state").

### 7. [LOW] `currentTask` plumbed from per-project, blocked by Finding 2

`HealthReporter.kt:60-62` reads the top-level `currentTask` from the
first dumb project's `ProjectIndexingState.currentTask`. Always `null`
per Finding 2. The deterministic "first dumb project in `openProjects`
order" ordering is a correct design choice, just non-observable today.

### 8. [LOW] `formatUptime` zero-padding inconsistent

`HealthReporter.kt:274-279` — leading `${days}` / `${hours}` /
`${minutes}` / `${seconds}` are bare; only the inner `%02d` fields
pad. Kdoc claims "stable column width to grep," which is false for
single-digit leading fields. Either pad the leading field or drop the
claim. Cosmetic.

### 9. [LOW] `MemoryUsageBlock.freeBytes = committed - used` is the wrong "free"

`HealthReporter.kt:287` documents `freeBytes` as committed-minus-used.
For an "is the IDE near OOM?" check, `max - used` (real headroom) is
the relevant signal. Agents will read "free" as "remaining before
OOM" — the `@McpDescription` doesn't distinguish. Fix: rename to
`committedFreeBytes` and add `headroomBytes = max - used` (computed
only when `max != -1L`), OR drop the field (agents have `used`/`max`).

### 10. [LOW] `loadClass` returning null — no log emission

The reflection chains silently degrade. When
`UnindexedFilesScannerExecutor` isn't found, `loadClass` returns
`null` with no log entry (line 159) — operationally invisible. Plan
explicitly said "log once" (plan line 165). Implementation only logs
from inner `getOrElse` catches, not the "class not found at all"
path. Add an `AtomicBoolean` latch + one-time `LOG.warn`.

### 11. [LOW] Tests don't exercise the "indexing in progress" branch

`HealthReporterIndexingPlatformTest.kt` only asserts steady
(non-dumb) state, plus reflection-doesn't-throw and empty-projects.
NO test forces `dumbMode=true` (e.g. via
`DumbModeTestUtils.runInDumbModeSynchronously`). Consequence:
Findings 1 & 2 wouldn't be caught even after the FQN fix because no
test ever asserts what `currentTask` / `scanningActive` SHOULD be
when something is happening. Add one fixture that flips dumb mode and
asserts both the top-level `dumbMode` and per-project `dumbModeActive`.

### 12. [LOW] `System.gc()` triggers verifier warning — worth a CLAUDE.md mention

`HealthReporter.kt:223-227` correctly `@Suppress("ExplicitGarbageCollectionCall")`
the call. JetBrains' Inspectopedia flags this and the Plugin Verifier
emits a warning. Given the `gcBeforeRead=false` default this is fine,
but warrants a CLAUDE.md "Known plugin-verifier warnings" entry so the
next contributor doesn't chase it.

## Threading & EDT model — OK with a kdoc fix
- No EDT bounce, no `ReadAction`, no PSI/VFS access.
- `DumbService.isDumb` and `StartupManager.postStartupActivityPassed`
  read from the coroutine thread without wrapping — Finding 3 covers
  the intentional-but-mis-documented trade-off.
- `ProjectManager.getOpenProjects()` returns a snapshot array; safe.
- `ManagementFactory.*` MXBeans are thread-safe per the JMX spec.

## Timeouts — OK
No `withTimeoutOrNull`, justified (MXBean reads + `System.gc()` hint
are <100 ms even on multi-GB heaps). Reflection probes can't hang —
all return immediately on ClassNotFound / NoSuchMethod.

## @McpDescription quality — matches plan, drifts from reality
5-section structure, trim-margin format — correct. But:
- `IndexingStatus.currentTask` / `queuedTasks` documented as
  "human-readable current dumb-mode task" / "tasks queued behind
  current one" — both permanently inert (Finding 2).
- `projectsIndexing[].scanningActive` documented as "True if
  `UnindexedFilesScannerExecutor` is scanning" — permanently false
  (Finding 1).

Either fix the implementation (preferred) or amend the descriptions
("currently always null/0 pending platform API access").

## Test coverage — memory excellent, indexing surface-only
- `HealthReporterMemoryTest` (12 tests, pure JUnit): heap+nonHeap
  shape, GC list non-empty, uptime tolerance, `gcBeforeRead=true`
  smoke, metaspace-pool-missing fallback, metaspace-pool-present
  plumbing, unbounded-max passthrough, `formatUptime` exhaustive.
  Solid; trust-able.
- `HealthReporterIndexingPlatformTest` (7 tests,
  `BasePlatformTestCase`): fresh-fixture-not-dumb, per-project shape,
  hash-filter happy/unknown, reflection-doesn't-throw,
  empty-projects-array, idempotency, `ProjectManager.openProjects`
  sanity. Missing: dumb-mode-active test (Finding 11), scanner-FQN-
  resolves assert (Finding 1).
- The "reflection-guarded probe does not throw" invariant the plan
  promises (plan line 192-193) is met only trivially — the probe runs
  and returns false; it would also pass with the FQN misspelled in
  every possible way.

## `mcp-integration.xml` registration — OK
`mcp-integration.xml:10` adds the toolset in the always-on
`<extensions>` block (no `<depends>` change). Placement correct:
`health.*` has no Java / Kotlin / mcpServer-conditional dependencies.
Sits between `PsiToolset` and `EditorToolset` (other reviews in this
branch placed new toolsets at the end of the list — cosmetic).

## Recommended actions before merge
1. **Finding 1** — fix the `UnindexedFilesScannerExecutor` FQN list
   (add `com.intellij.openapi.project.UnindexedFilesScannerExecutor`
   as the first entry). Add a `Class.forName` resolution test.
2. **Finding 2** — decide: drop `currentTask` / `queuedTasks` from
   the model, OR wire to a real source (`DumbServiceMergingTaskQueue`
   private field). Update `@McpDescription` accordingly.
3. **Finding 3** — rewrite the `isDumb` kdoc paragraph
   (`HealthReporter.kt:22`).
4. **Findings 10 / 11** — log-once on the reflection-class-missing
   path; add a `DumbModeTestUtils.runInDumbModeSynchronously` test
   that flips `dumbMode` and asserts the snapshot.
5. **Finding 5** — drop the redundant `.let { … }`.
6. **Finding 6** — short-circuit the project-list filter before
   `snapshotProject`.
7. Findings 4, 8, 9, 12 are nits.

## File / line references
- `core/HealthReporter.kt:22` — false "volatile-read; thread-safe"
  kdoc (Finding 3)
- `core/HealthReporter.kt:55-68` — filter inefficiency (Finding 6) +
  redundant `.let` (Finding 5)
- `core/HealthReporter.kt:60-62` — top-level `currentTask`
  (Finding 7, blocked by Finding 2)
- `core/HealthReporter.kt:109-129` — `currentTaskText` reflection
  (Finding 2)
- `core/HealthReporter.kt:131-147` — `queuedTasksFor` reflection
  (Finding 2)
- `core/HealthReporter.kt:154-185` — `scannerRunning` wrong FQNs
  (Finding 1)
- `core/HealthReporter.kt:187-196` — `loadClass` no-log on miss
  (Findings 4, 10)
- `core/HealthReporter.kt:223-227` — `System.gc()` (Finding 12)
- `core/HealthReporter.kt:266-280` — `formatUptime` padding
  (Finding 8)
- `core/HealthReporter.kt:282-289` — `freeBytes` semantics
  (Finding 9)
- `model/HealthInfo.kt:39` — `scanningActive` permanently false
  (Finding 1)
- `model/HealthInfo.kt:20-23` — `currentTask` / `queuedTasks`
  permanently null/0 (Finding 2)
- `tools/HealthToolset.kt:10-93` — `@McpDescription` drifts
  (Findings 1, 2)
- `META-INF/mcp-integration.xml:10` — toolset registration (OK)
- `test/.../HealthReporterMemoryTest.kt` — solid coverage
- `test/.../platform/HealthReporterIndexingPlatformTest.kt` — missing
  dumb-mode-active test (Finding 11)

## Sources consulted
- Plan `docs/plans/health-group.md`
- [DumbService.kt — JetBrains/intellij-community master](https://github.com/JetBrains/intellij-community/blob/master/platform/core-api/src/com/intellij/openapi/project/DumbService.kt)
  — kdoc on `isDumb` thread-safety; absence of `getCurrentTask` /
  `queuedTasksCount`
- [UnindexedFilesScannerExecutor.kt — JetBrains/intellij-community master](https://github.com/JetBrains/intellij-community/blob/master/platform/platform-impl/src/com/intellij/openapi/project/UnindexedFilesScannerExecutor.kt)
  — package `com.intellij.openapi.project`, `isRunning: StateFlow<Boolean>`,
  `getInstance(Project)` static factory
- [DumbServiceImpl.kt — JetBrains/intellij-community master](https://github.com/JetBrains/intellij-community/blob/master/platform/platform-impl/src/com/intellij/openapi/project/DumbServiceImpl.kt)
  — internal task queue is private `DumbServiceMergingTaskQueue`
- [StartupActivityTracker.kt — canonical use of `postStartupActivityPassed`](https://github.com/JetBrains/intellij-community/blob/master/platform/platform-impl/src/com/intellij/ide/startup/StartupActivityTracker.kt)
- [Call to System.gc() — JetBrains Inspectopedia](https://www.jetbrains.com/help/inspectopedia/CallToSystemGC.html)
- [Threading Model — IntelliJ Platform Plugin SDK](https://plugins.jetbrains.com/docs/intellij/threading-model.html)
- [What is Compressed Class Space? — stuefe.de](https://stuefe.de/posts/metaspace/what-is-compressed-class-space/) — confirms `Metaspace` substring filter doesn't collide with `Compressed Class Space`
