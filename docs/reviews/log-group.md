# Review: log.tail + log.errors_since (`log.*` group)

Branch: `claude/project-features-analysis-odEwP` @ `fe3a239` HEAD.
Salvage commit `3a0985f` + green-fix `733906d`. Scope: `core/LogReader.kt`,
`model/LogInfo.kt`, `model/args/LogArgs.kt`, `tools/LogToolset.kt`, the
META-INF registration, and the two new test files.

## Verdict
Needs changes before merge. Shape is right — efficient 1 MB reverse-seek
tail for `log.tail`, `LogReader` ctor takes injected path + rotation
supplier so tests can use `TemporaryFolder`, redaction on by default with
`redacted=true` audit flag, both fixer notes verified
(`stripThrowablePrefix` at `LogReader.kt:368-372` and `%02d` at
`LogReaderTest.kt:290-292`) — but `errorsSince` reads from **byte 0 of
every source up to an 8 MB cap**, so on a multi-MB IDE log a query for
"errors in the last 5 minutes" parses the OLDEST 8 MB and returns
empty/stale data; the per-line regex timeout uses `Thread.interrupt()`,
which the JDK documents as ineffective against `Matcher.find()` and will
leak CPU-burning daemon threads under ReDoS; `log.tail` with `severity` set
silently drops the stacktrace continuation lines the description says are
returned as `parsed=false`; and the rotation walk runs unconditionally
even when the cutoff is one minute ago.

## Top finding
**Finding 1 (HIGH)** — `errorsSince` reads `idea.log` from offset 0 (not
from the end), so on a multi-MB IDE log the primary use case ("any errors
in the last 5 minutes?") returns empty/stale data. The plan requires a
reverse-seek tail with cutoff-aware rotation walking; this implementation
walks forward from the head of every source file.

## Summary
- Four source files (`LogReader.kt` 557 LOC, `LogInfo.kt` 63 LOC,
  `LogArgs.kt` 22 LOC, `LogToolset.kt` 155 LOC), one META-INF line, two
  JUnit-4 + `TemporaryFolder` test files (`LogReaderTest.kt` 299 LOC,
  `LogReaderTailTest.kt` 200 LOC).
- `@McpDescription` strings match the plan verbatim, 5-section convention.
- Hard rules OK: `TOOL_TIMEOUT_MS = 10_000L` via `withTimeoutOrNull`
  (`LogToolset.kt:67,132,153`). No EDT, no `ReadAction`.
  `kotlinx-serialization-json` policy untouched.
- Both fixer notes present and correct.
- `truncateUtf8` from `util/Utf8Truncation.kt` is NOT used — byte cap is
  done line-aligned in `capLogTailResponseBytes` (`LogReader.kt:463-475`).
  Fine because lines are kept whole.

## Findings

### 1. [HIGH] `errorsSince` reads from byte 0 of every source — recent-history queries return ancient or empty data

`LogReader.kt:137-149`:
```kotlin
for (src in sources) {
    if (byteBudget <= 0) { truncatedBudget = true; break }
    val text = readUpTo(src, byteBudget) ?: continue   // reads from offset 0
    byteBudget -= text.bytesRead
    if (text.truncated) truncatedBudget = true
    ...
}
```

`readUpTo` (`LogReader.kt:222-246`) seeks to 0 and reads
`min(len, maxBytes)`. Concretely: IDE runs for two days, `idea.log` is 60
MB. Caller does `log.errors_since(lastMinutes=5)`. We read bytes 0–8 MB
from the START — day 1, hour 1. Every entry pre-dates the cutoff;
`sinceCutoff.filter` drops them all; result is `errors=[]`, `total=0`,
`truncated=true`. Real recent errors at offsets ~52 MB+ are never read.

Plan (Threading & timeout) and edge case #3 promise reverse-seek (1 MB
`MAX_TAIL_BYTES`) with rotations walked "as needed" only when the cutoff
predates the current file's first line. The implementation does the
opposite: reads the HEAD of every file, exhausts the 8 MB budget on
ancient history, never reaches the recent entries.

Fix: `readTail(current, MAX_TAIL_BYTES)` first (same as `tail()`). If the
tail's oldest line is `< cutoff`, you have all you need (no rotation
walk). Else walk rotations newest-first (`.1`, `.2`, …) and stop once a
rotation's last line is older than the cutoff. The 8 MB cap stays as a
safety net.

This is the highest-impact correctness bug in the PR; the tool's primary
use case is broken on any long-lived IDE session.

### 2. [HIGH] Per-line regex timeout via `Thread.interrupt()` is ineffective — leaks CPU-burning threads under ReDoS

`LogReader.kt:401-414`:
```kotlin
private fun regexFindWithTimeout(pattern: Pattern, input: String): Boolean {
    val matcher = pattern.matcher(input)
    val t = thread(start = true, isDaemon = true, name = "log-regex") {
        result.set(runCatching { matcher.find() }.getOrElse { false })
    }
    t.join(REGEX_LINE_TIMEOUT_MS)
    if (t.isAlive) { t.interrupt(); return false }
    return result.get() == true
}
```

JDK documents `Matcher.find()` as NOT interruptible (known "Won't Fix"
RFE). `Thread.interrupt()` sets the flag, `Matcher` never checks it, the
daemon thread keeps burning a CPU core for as long as the regex takes
(can be minutes). We return `false` to the caller, but the thread is
alive and consuming CPU.

Attack: caller passes `regex="(a+)+b"`, `lines=5000`, tail contains 500
long all-a lines. We spawn 500 daemon threads in <25 s; each runs for
minutes. IDE CPU saturates, GC pressure rises, unrelated coroutines
starve. Test at `LogReaderTest.kt:104-113` verifies we return 0 lines but
does NOT verify thread termination.

Fix: use `InterruptibleCharSequence` (OCPsoft pattern). Wrap input in a
`CharSequence` that throws on every `charAt()` past a deadline — the
matcher reads characters in its inner loop, so the throw escapes within
microseconds. No extra threads, no leak. Or reject patterns containing
nested quantifiers at compile time. Current implementation is worse than
no timeout because callers think they're protected.

### 3. [HIGH] `log.tail` with `severity` filter drops stacktrace continuation lines — description says they're returned as `parsed=false`

`LogReader.kt:248-266`:
```kotlin
if (minSeverityIdx >= 0) {
    val s = line.severity ?: return false       // drops parsed=false
    if (severityIndex(s) < minSeverityIdx) return false
}
```

`LogToolset.kt:39-41` description:
> Stacktrace continuation lines are separate LogLine entries with
> parsed=false; for grouping use log.errors_since.

With `severity="WARN"`, the WARN header is returned but every
`\tat com.…` / `Caused by:` line has `severity=null` and is silently
dropped at line 254. Agent gets the throwable class but no stack frames.
The "use log.errors_since" fallback exists, but the description promises
continuations come through as separate `parsed=false` entries — they
don't, once a severity filter is set.

Fix: either amend `@McpDescription` ("severity filter implicitly drops
parsed=false continuations") or sticky-pass `parsed=false` lines that
follow a matched line. (a) is one-line; (b) is correct.

### 4. [MED] Rotation walk runs unconditionally — wasted I/O for short-window queries

`LogReader.kt:178-183` — `collectErrorSources` returns
`rotations.reversed() + main` regardless of cutoff. Plan says
"as needed … when the cutoff predates the current log's first line".
Not implemented. Falls out from Finding 1's fix.

### 5. [MED] `log_errors_since` timeout fallback drops the computed `since` echo

`LogToolset.kt:141-146`:
```kotlin
} ?: LogErrorsSinceResponse(
    since = sinceIsoTimestamp ?: "",
    errors = emptyList(), total = 0, truncated = true,
)
```

When caller passed only `lastMinutes`, `since=""` — losing the timestamp
the happy path computes via `parseCutoff` (`LogReader.kt:122`). Caller
can't distinguish "no errors found" from "we timed out". Pre-compute
`cutoff` outside `withTimeoutOrNull` and pass the formatted ISO into the
fallback.

Bonus: `LogErrorsSinceResponse` has no `logPath` field at all (compare
`LogTailResponse.logPath`). Two MCP tools, same plumbing, inconsistent
UX. Add `logPath: String` to mirror the tail response.

### 6. [MED] `totalLinesScanned` semantics undocumented

`LogReader.kt:75-90` — `totalLinesScanned` = lines in the 1 MB tail
buffer (pre-filter). Field is a bare `Int` with no doc in `LogInfo.kt`
or the `@McpDescription`. Could mean "lines in file", "lines we read",
or "lines that survived filters". Document the chosen semantic.

### 7. [LOW] `redact()` patterns have an unused capture group

`LogReader.kt:526-535` — `token=([A-Za-z0-9._\-]{16,})` captures the
token but the replacement `token=***REDACTED***` doesn't backreference.
The parens are pointless. Cosmetic; no security impact.

### 8. [LOW] `THROWABLE_FQN` requires a dot — bare exception names not lifted

`LogReader.kt:513` — `[A-Za-z_$][\w$.]*\.[A-Z][\w$]*` requires `.` before
the capital. `RuntimeException: boom` (no package) won't match. Platform
typically logs FQNs so OK; mention in docs.

### 9. [LOW] No test for "exactly 1 MB file with no trailing newline"

`LogReader.kt:71` — `if (tailText.truncated && rawLines.isNotEmpty())
rawLines.drop(1)` — correctness depends on the equality boundary at
`len == MAX_TAIL_BYTES`. Tests cover `>1 MB` and `<1 MB`; the exact-1-MB
edge isn't exercised.

### 10. [LOW] `LogReader.tail` UTF-8 decode silent-replaces incomplete trailing bytes with U+FFFD on concurrent writes

`String(buf, UTF_8)` decoder silently substitutes U+FFFD for incomplete
sequences. `drop(1)` handles the START boundary; the END boundary is
clean only for static files. Mid-write race not exercised. Low-prio for
`idea.log` (rare 60 MB mid-tail writes).

### 11. [LOW] Redaction coverage gap: secret in `stacktrace`, multiple secrets per line, `Caused by` chain with embedded secret

`LogReaderTest.kt` Section 4 tests a single Bearer in an `INFO` line.
No test covers redaction on `ErrorEntry.stacktrace` (Bearer in
`\tat com.foo.Net…` continuation) — code path exists
(`LogReader.kt:455`) but is untested. 15-min addition; closes the
security-coverage gap.

### 12. [LOW] Implicit invariant on rotation order — silent regression risk

`LogReader.kt:181-183` returns `rotations.reversed() + main` assuming `.1`
is newer than `.5` (standard log4j behaviour). If JetBrains changes
rotation semantics, results return out of chronological order with no
warning. Cheap fix: stable-sort all parsed lines by timestamp before
grouping.

## Threading & EDT model — OK
Pure file I/O. No EDT, no `ReadAction`, no PSI. No `Dispatchers.IO`
bounce (plan said "not required" — `RandomAccessFile` is blocking but
cheap on 1 MB).

## Timeouts — OK at outer boundary, BROKEN at the regex boundary
- `withTimeoutOrNull(10_000L)` per tool call.
- `MAX_TAIL_BYTES = 1 MB`, `MAX_ROTATION_BYTES = 8 MB`, `MAX_ROTATIONS = 5`
  match plan.
- `REGEX_LINE_TIMEOUT_MS = 50L` — exists but ineffective (Finding 2).

## @McpDescription quality — mostly OK
Both descriptions match the plan verbatim. Drift items:
- `log.tail` claims continuations come through as `parsed=false` — true
  only without a severity filter (Finding 3).
- `log.errors_since` says rotations walked "as needed" — they're walked
  always (Finding 4).

## Test coverage — present, structurally good, misses Finding 1's failure mode
- `LogReaderTest.kt` (16 tests) covers parsing, filters, grouping,
  redaction, cutoff, limit/total.
- `LogReaderTailTest.kt` (7 tests) covers 3 MB tail, missing file,
  zero-byte, emoji boundary, rotation walk, 8 MB budget.
- **Missing**: `errorsSince` with `lastMinutes=5` against a current log
  larger than the 8 MB budget — would catch Finding 1.
- **Missing**: thread-leak assertion on the regex-timeout test (Finding 2).
- **Missing**: `tail(severity=…)` continuation-line behaviour (Finding 3).
- **Missing**: 1 MB file edge case (Finding 9); stacktrace redaction
  (Finding 11).

## Recommended actions before merge
1. **Finding 1** — switch `errorsSince` to reverse-seek the current log,
   walk rotations only when cutoff predates the current file's first
   line. Add the "recent errors in a huge log" test that demonstrates
   the fix.
2. **Finding 2** — replace `Thread.interrupt()` with
   `InterruptibleCharSequence`. Add `Thread.activeCount` assertion to
   `LogReaderTest.kt:104-113`.
3. **Finding 3** — amend `@McpDescription` OR pass `parsed=false`
   continuations sticky with the preceding matched line.
4. **Finding 5** — pre-compute `since` outside `withTimeoutOrNull`.
5. **Finding 4** — falls out from Finding 1.
6. Findings 6–12 are nits.

## File / line references
- `core/LogReader.kt:137-149` — forward-from-byte-0 rotation walk (Finding 1)
- `core/LogReader.kt:222-246` — `readUpTo` (forward read, no seek)
- `core/LogReader.kt:401-414` — `regexFindWithTimeout` (Finding 2)
- `core/LogReader.kt:254-256` — severity filter drops `parsed=false` (Finding 3)
- `core/LogReader.kt:178-183` — `collectErrorSources` unconditional (Finding 4)
- `core/LogReader.kt:368-372` — `stripThrowablePrefix` (fixer note verified)
- `core/LogReader.kt:463-475` — line-aligned byte cap (`Utf8Truncation` unused)
- `core/LogReader.kt:513` — `THROWABLE_FQN` (Finding 8)
- `core/LogReader.kt:526-535` — redaction patterns (Finding 7)
- `tools/LogToolset.kt:141-146` — `errors_since` timeout fallback (Finding 5)
- `model/LogInfo.kt:56-62` — `LogErrorsSinceResponse` lacks `logPath` (Finding 5)
- `test/.../LogReaderTest.kt:290-292` — `%02d` fix (fixer note verified)
- `test/.../LogReaderTailTest.kt:128-180` — rotation tests (Finding 12)

Sources consulted:
- Plan `docs/plans/log-group.md`
- [PathManager.java (JetBrains/intellij-community)](https://github.com/JetBrains/intellij-community/blob/master/platform/util/src/com/intellij/openapi/application/PathManager.java) — `getLogPath()` returns directory not file
- [How to interrupt a long-running infinite Java regular expression (OCPsoft)](https://www.ocpsoft.org/regex/how-to-interrupt-a-long-running-infinite-java-regular-expression/) — `InterruptibleCharSequence`
- [Runaway Regular Expressions: Catastrophic Backtracking](https://www.regular-expressions.info/catastrophic.html)
- [Logs (IntelliJ IDEA Documentation)](https://www.jetbrains.com/help/idea/setting-log-options.html)
- [Locating IDE log files (JetBrains support)](https://intellij-support.jetbrains.com/hc/en-us/articles/207241085-Locating-IDE-log-files)
