# Review: exec.compile_check

Commit reviewed: `733906d` on `claude/project-features-analysis-odEwP`.

## Verdict
Needs changes (non-blocking for shipping, but the central design contract is
unmet). The tool runs and returns a usable boolean, but the "rich diagnostics
with line/column/severity/factoryId" promise made by both the plan and the
response model is delivered in name only — every failure path collapses to a
single ERROR (or FATAL) entry. The premise that drove the chosen
implementation — "`JvmScriptCompiler.ResultWithDiagnostics` no longer exists in
Kotlin 2.1.20" — is also incorrect; the modular API is present in the
embeddable jar and would actually meet the plan.

## Summary
`exec.compile_check` is wired correctly end-to-end (toolset → `KotlinCompileOnly`
→ JSR-223 `Compilable.compile`), respects the 10 s cap, skips confirmation/AST
checks per plan, and has a clean test seam. The shortfall is diagnostic
fidelity: the JSR-223 surface throws a single `ScriptException` per failure, so
multi-error snippets and any warnings disappear before they reach the agent. The
fixer's switch from `BasicJvmScriptingHost` to JSR-223 was based on an
incorrect premise (see "Diagnostic-fidelity assessment" below); the original
plan's API path is still viable on Kotlin 2.1.20.

## Findings

1. **MAJOR — Response advertises fields it cannot populate**
   (`model/CompileCheckInfo.kt:23`, `tools/ExecToolset.kt:186–197`,
   `exec/KotlinCompileOnly.kt:118–143`). The `@McpDescription` and the
   `CompileDiagnostic` data class advertise
   `severity: FATAL|ERROR|WARNING|INFO|DEBUG`, `factoryId`, and multiple
   entries. The implementation never emits WARNING/INFO/DEBUG, never sets
   `factoryId`, and returns at most one diagnostic per compile attempt
   (one `ScriptException` → one `CompileDiagnostic`). Fix: either (a) migrate
   to the modular API (see assessment below) and actually populate these
   fields, or (b) shrink the model/description to match reality
   (`severity: "ERROR" | "FATAL"`, drop `factoryId`, document
   "single-diagnostic-per-call until v1.1"). Lying about the shape is the
   worst outcome — clients will write switches over severities that never
   fire.

2. **MAJOR — Premise behind the rewrite is wrong**
   (`exec/KotlinCompileOnly.kt:23–27` "lost access to … `JvmScriptCompiler`").
   `org.jetbrains.kotlin:kotlin-scripting-jvm-host:2.1.20` still ships
   `kotlin.script.experimental.jvmhost.JvmScriptCompiler` with
   `suspend operator fun invoke(SourceCode, ScriptCompilationConfiguration):
   ResultWithDiagnostics<CompiledScript>`. It's a `suspend operator fun`
   rather than a `fun compile(...)`, which may be why the fixer thought it
   was gone. Both jars (`kotlin-scripting-jsr223` already pulls
   `kotlin-scripting-jvm-host` transitively) are on the runtime classpath.
   Fix: add the `kotlin-scripting-jvm-host` import and call the operator
   directly — `runBlocking { compiler(source, config) }` returns the full
   `reports: List<ScriptDiagnostic>` regardless of success/failure.

3. **MAJOR — `withTimeoutOrNull` cannot cancel an in-flight Kotlin compile**
   (`exec/KotlinCompileOnly.kt:53–55`). The compile runs inside
   `withContext(Dispatchers.IO) { compileFn(source) }` where `compileFn` is a
   plain blocking `Compilable.compile(...)`. Kotlin coroutine cancellation
   only fires at suspension points; the compiler has none. On timeout the
   coroutine "returns" `null` but the compiler thread keeps running and keeps
   consuming a CPU until it finishes — a 30 s pathological compile from a
   misbehaving snippet will hold a thread for the full 30 s after we return
   `ok=false`. Fix: wrap the blocking call in `runInterruptible { … }` so
   cancellation translates to `Thread.interrupt()`, or use a `Future` +
   `Future.cancel(true)` pattern like `KotlinExecutor` already does
   (`KotlinExecutor.kt:69–103`). Even better when migrating to the modular
   API: the `suspend operator fun invoke` is genuinely cancellable.

4. **MAJOR — Cold-start cost has no caching despite the open question being
   answered "yes"** (`exec/KotlinCompileOnly.kt:95–97`). Plan ("Open
   questions / risks") explicitly resolves "Cache the `JvmScriptCompiler`
   across calls? Yes — amortises the ~3 s cold start to ~200 ms warm." The
   implementation builds a fresh `ScriptEngineManager` + engine on every
   call. With JSR-223 this is the right call (engine retains classloader
   state across compiles → leak risk like `KotlinExecutor` solved), but the
   user-visible 3 s cold start happens on *every* call, not just the first.
   Fix when migrating to the modular API: cache the
   `BasicJvmScriptingHost`+`JvmScriptCompiler` pair as `by lazy {}` in the
   object — those are stateless across invocations.

5. **MINOR — Line/column with `wrap=true` are wrapper-relative, no offset
   constant** (`exec/CodeWrapper.kt:18–43`, `tools/ExecToolset.kt:182–185`).
   The plan's v1.1 mitigation ("subtract a `CodeWrapper.userCodeStartLine`
   constant") is acknowledged in the description but not even half-built —
   `CodeWrapper` has no such constant. Today an agent told that line 42 has
   an unresolved reference will look at user-code line 42 (which is line 22
   of their snippet, because the wrapper prepends 20 lines). The
   description warns about this but only at the level of "subtract the
   offset client-side" without telling them the offset. Fix: add
   `const val USER_CODE_OFFSET_LINES = 20` (or actually count the lines
   `wrap()` emits before the user `$userCode` interpolation point) to
   `CodeWrapper` so clients have an authoritative value, and reference it
   in the `@McpDescription`.

6. **MINOR — `wrap=true` does not actually compile in `testScripting`**
   (`src/testScripting/.../KotlinCompileOnlyTest.kt:41–53,73–79`). Three
   tests are `@Ignore`d because `testScripting` excludes the IntelliJ
   platform classpath — but `wrap=true` is the *default* of the tool, the
   one users will hit 99% of the time, and we have zero automated coverage
   of it. The ignore is justified given the source-set design, but the
   gap should be filled by either (a) a `BasePlatformTestCase` integration
   test that uses a fake CompileFn injected via the existing seam plus a
   real wrap-vs-no-wrap comparison, or (b) a `runIde` smoke test
   automated via the JetBrains Gradle IntelliJ plugin's `runIde --headless`
   option. Right now the tool's default codepath is untested. Cap-tests #3
   and #4 cover only `wrap=false`.

7. **MINOR — `KotlinCompileOnly.check(...)` swallows the inner exception
   class for `IllegalStateException` etc.** (`exec/KotlinCompileOnly.kt:57–69`).
   The outer `try { withTimeoutOrNull { … } } catch (t: Throwable)` is
   unreachable in practice because the inner `doCompile` already catches
   `Throwable`. Only `TimeoutCancellationException` from
   `withTimeoutOrNull` could leak out, but `withTimeoutOrNull` swallows it
   and returns `null` instead. The outer try is dead code. Fix: remove it,
   or convert to `coroutineScope { withTimeout(...) }` if you want to
   distinguish "timed out" from "cancelled by caller".

8. **MINOR — `getEngineByName("kotlin")` vs `getEngineByExtension("kts")`**
   (`exec/KotlinCompileOnly.kt:97` vs `KotlinExecutor.kt:136`). The sibling
   executor uses `getEngineByExtension("kts")` with `getEngineByName("kotlin")`
   as fallback. `KotlinCompileOnly` uses only `getEngineByName("kotlin")`,
   which works in 2.1.20 but is the less-portable lookup key — the
   META-INF/services registration historically pivoted on the extension.
   Align with `KotlinExecutor.obtainEngine` for consistency, so failures
   manifest the same way.

9. **MINOR — `factoryId` is in the model but never populated** (effectively a
   sub-point of finding #1). The plan promised `code` field mapping from
   `ScriptDiagnostic.code`, used as "factory id" so clients can match
   `UNRESOLVED_REFERENCE` etc. JSR-223 doesn't expose this. If you keep
   JSR-223, drop the field; if you migrate, populate it.

10. **NIT — Doc says "diagnostics carry positions" but `file` is the
    `ScriptException.fileName` which is typically `null`**
    (`exec/KotlinCompileOnly.kt:128`). On the JSR-223 path you'll never get
    a meaningful `file`; the doc says "synthetic name of the wrapped
    script". Either set it to `"<wrapped>"` or `CodeWrapper.GENERATED_CLASS`
    so clients can disambiguate, or document the always-null reality.

11. **NIT — `private fun synthesize` is `@Suppress("unused")`** in the test
    file (`KotlinCompileOnlyTest.kt:132–134`). Either use it or delete it —
    dead test scaffolding rots.

## Diagnostic-fidelity assessment

**What the plan promised** (docs/plans/exec-compile-check.md lines 76–87,
123–144):

- Multiple `ScriptDiagnostic` entries per call (errors + warnings + info).
- `severity ∈ {FATAL, ERROR, WARNING, INFO, DEBUG}` from
  `ScriptDiagnostic.Severity`.
- `line`, `column` from `ScriptDiagnostic.location.start`.
- `factoryId` from `ScriptDiagnostic.code` (e.g. `"UNRESOLVED_REFERENCE"`).
- All of this via `BasicJvmScriptingHost` + `JvmScriptCompiler.compile(...)`
  → `ResultWithDiagnostics<CompiledScript>`.

**What JSR-223 `Compilable.compile()` actually delivers** (current
implementation):

- One diagnostic per call (synthesised from the single
  `ScriptException`).
- Severity collapsed to literal string `"ERROR"` (or `"FATAL"` on
  non-`ScriptException` `Throwable`); WARNING/INFO/DEBUG are unreachable.
- `line`/`column` from `ScriptException.lineNumber/columnNumber` — these are
  populated *only* if the underlying Kotlin scripting code constructs the
  exception with positions (which it does for compile errors, but not for
  all paths).
- `factoryId` always null.
- Warnings on successful compiles: invisible (the `compile()` call simply
  returns the engine; warnings reported during compilation are dropped on
  the floor by `KotlinJsr223JvmScriptEngineBase`).

**What the modular `kotlin-scripting-jvm-host` API would deliver, on Kotlin
2.1.20**:

- The original plan, verbatim. `JvmScriptCompiler` exists in
  `kotlin-scripting-jvm-host:2.1.20` (same artifact group, transitive
  dependency of `kotlin-scripting-jsr223`, already on our runtime classpath):
  ```kotlin
  open class JvmScriptCompiler(
      baseHostConfiguration: ScriptingHostConfiguration = …,
      compilerProxy: ScriptCompilerProxy? = null,
  ) : ScriptCompiler {
      override suspend operator fun invoke(
          script: SourceCode,
          scriptCompilationConfiguration: ScriptCompilationConfiguration,
      ): ResultWithDiagnostics<CompiledScript>
  }
  ```
- `ResultWithDiagnostics` always carries `reports: List<ScriptDiagnostic>`
  — both on `Success` and `Failure`, so warnings on a successful compile
  are preserved.
- `ScriptDiagnostic`: `severity: Severity` (enum), `message: String`,
  `code: Int`, `location: SourceCode.Location?` with
  `start: SourceCode.Position(line, col)`, `sourcePath: String?`,
  `exception: Throwable?`.

**Concrete migration recommendation** — ~40 LoC change, no Gradle changes:

```kotlin
private val host: BasicJvmScriptingHost by lazy { BasicJvmScriptingHost() }
private val compiler: JvmScriptCompiler by lazy { JvmScriptCompiler() }
private val baseConfig by lazy {
    createJvmCompilationConfigurationFromTemplate<SimpleScriptTemplate> {
        jvm { dependenciesFromClassContext(KotlinCompileOnly::class,
                                            wholeClasspath = true) }
    }
}

private suspend fun doCompile(source: String): CompileOutcome {
    val src = source.toScriptSource(name = "compile-check.kts")
    val result = compiler(src, baseConfig)          // ResultWithDiagnostics
    val diags = result.reports
        .filter { it.severity.ordinal >= ScriptDiagnostic.Severity.INFO.ordinal }
        .map { d -> CompileDiagnostic(
            severity = d.severity.name,
            line = d.location?.start?.line,
            column = d.location?.start?.col,
            file = d.sourcePath,
            message = d.message,
            factoryId = d.code.takeIf { it != 0 }?.toString(),
        ) }
    val ok = result is ResultWithDiagnostics.Success
    return CompileOutcome(ok = ok, diagnostics = diags)
}
```

Plus update `check(...)` to wrap the suspend call in
`runInterruptible(Dispatchers.IO) { runBlocking { … } }` to keep timeout
cancellation honest. The `SimpleScriptTemplate` (any annotated `@KotlinScript`
class — the simplest one in the repo or a 5-line declaration) is needed for
`createJvmCompilationConfigurationFromTemplate`; we can declare it as a
private `@KotlinScript class CompileCheckScript : Any()` in the same file.

This buys: real WARNINGs, multi-error reports, position info for every
diagnostic, factory codes — i.e. the v1 contract.

## Plan-vs-implementation gaps

| Plan section | Implementation status |
|---|---|
| `BasicJvmScriptingHost` + `JvmScriptCompiler` primary path | NOT implemented — JSR-223 only |
| `ScriptDiagnostic` → multi-entry mapping | NOT implemented — collapsed to 1 entry |
| WARNING / INFO / DEBUG severities | NOT implemented — only ERROR / FATAL emitted |
| `factoryId` from `ScriptDiagnostic.code` | NOT implemented — always null |
| `dependenciesFromClassContext(..., wholeClasspath = true)` | NOT implemented — JSR-223 default classpath |
| `CodeWrapper.userCodeStartLine` offset constant | NOT implemented (deferred to v1.1 per description but not present) |
| Cache the compiler across calls | NOT implemented (fresh engine per call) |
| `withTimeoutOrNull(10_000) { ... }` 10 s cap | Implemented |
| No `ExecSettings.enabled` gate / no confirmation / no AST check / no audit | Correctly implemented (`ExecToolset.kt:207–212`) |
| `wrap=true` reuses `CodeWrapper.wrap` verbatim | Implemented (`KotlinCompileOnly.kt:51`) |
| Test seam for unit tests | Implemented (`check(...)` 4-arg overload) |
| Isolated `testScripting` source set | Implemented (`build.gradle.kts:150–184`) |

## Research notes (with URLs)

- `JvmScriptCompiler` in Kotlin 2.1.20 — confirmed exists with
  `suspend operator fun invoke(...): ResultWithDiagnostics<CompiledScript>`:
  https://github.com/JetBrains/kotlin/blob/v2.1.20/libraries/scripting/jvm-host/src/kotlin/script/experimental/jvmhost/jvmScriptCompilation.kt
- `BasicJvmScriptingHost` in Kotlin 2.1.20 — does NOT expose `compile()`
  directly, only `eval()` (the original plan was slightly wrong here — you
  call `JvmScriptCompiler` directly, not the host):
  https://github.com/JetBrains/kotlin/blob/v2.1.20/libraries/scripting/jvm-host/src/kotlin/script/experimental/jvmhost/BasicJvmScriptingHost.kt
- `ScriptDiagnostic` fields (`severity`, `code`, `message`, `location`,
  `sourcePath`, `exception`):
  https://github.com/JetBrains/kotlin/tree/v2.1.20/libraries/scripting/common/src/kotlin/script/experimental/api
- Kotlin scripting tutorial (compile-only pattern):
  https://kotlinlang.org/docs/custom-script-deps-tutorial.html
- `State of Kotlin Scripting 2024` — `kotlin-scripting-jsr223` is in
  maintenance mode; `kotlin-scripting-jvm-host` is the recommended path:
  https://blog.jetbrains.com/kotlin/2024/11/state-of-kotlin-scripting-2024/
- `withTimeoutOrNull` does NOT interrupt non-suspending blocking code;
  must use `runInterruptible` to translate to `Thread.interrupt()`:
  https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/with-timeout-or-null.html
- `Dispatchers.IO` does not clear interrupt status between invocations —
  another reason to favour `runInterruptible`:
  https://github.com/Kotlin/kotlinx.coroutines/issues/2771
- Kotlin scripting examples (compile-then-eval split pattern):
  https://github.com/Kotlin/kotlin-script-examples

## Test coverage assessment

What `testScripting` actually covers today (5 active tests; 3 ignored):

- `syntax error` (wrap=false) — real compiler, real diagnostic
- `unresolved reference` (wrap=false) — real compiler, real diagnostic
- `timeout` — fake compile via test seam, validates response shape
- `compiler crash` — fake compile, validates FATAL synthesis
- Conspicuously absent: any test of `wrap=true`, any test asserting
  multiple diagnostics, any test asserting severity ≠ "ERROR".

Gaps to fill (in priority order):

1. End-to-end `wrap=true` test — requires a `BasePlatformTestCase` or a
   shim that supplies stubs for `Project` / `Disposable` / `ReadAction` /
   `ApplicationManager` on the JSR-223 compile classpath. A 30-LoC stub
   module wired into `testScripting`'s `compileClasspath` would unblock
   all three ignored tests.
2. Position-fidelity test: feed a known-bad snippet whose error is on a
   specific line, assert `line` matches. Currently we assert "at least one
   ERROR" but not whether positions are right — which is the *whole point*
   of the tool.
3. Multi-error test: a snippet with two unresolved references should
   produce two diagnostics. Today it produces one (because JSR-223
   throws on the first), and we have no test catching that regression
   even if we fix the underlying issue.
4. Warning-only test: a deprecated API call should yield `ok=true` with
   one WARNING diagnostic. Today this is unreachable; should pass after
   modular-API migration.

## Cross-cutting suggestions

- Cache heavy state (compiler, config, host) as `by lazy` and preheat from
  a `StartupActivity` so the first agent call doesn't eat 3 s of cold
  start — same trick would help `KotlinExecutor`.
- After the modular migration, extract a shared `ScriptHost` used by both
  `KotlinCompileOnly` and `KotlinExecutor` ("compile via `JvmScriptCompiler`,
  evaluate via `BasicJvmScriptEvaluator`"), tightening the
  "compile-passing implies execute-passing" invariant the plan headlined.
- Add `Logger.getInstance(KotlinCompileOnly::class.java).debug(...)` at
  start/end of `doCompile` — the plan called for optional debug logging
  and bug reports about wrong line numbers will want the wrapped source
  and raw diagnostic list.
- Once positions are accurate, auto-subtract
  `CodeWrapper.USER_CODE_OFFSET_LINES` from diagnostics inside the
  user-code window and demote wrapper-preamble diagnostics to debug log
  (those are wrapper bugs the user can't fix).

## References

- `src/main/kotlin/com/github/xepozz/ide/introspector/exec/KotlinCompileOnly.kt` —
  reviewed implementation (148 LoC).
- `src/main/kotlin/com/github/xepozz/ide/introspector/model/CompileCheckInfo.kt` —
  response model.
- `src/main/kotlin/com/github/xepozz/ide/introspector/model/args/ExecArgs.kt` —
  `CompileCheckArgs`.
- `src/main/kotlin/com/github/xepozz/ide/introspector/tools/ExecToolset.kt:146–212`
  — `@McpTool` entry point.
- `src/main/kotlin/com/github/xepozz/ide/introspector/exec/CodeWrapper.kt` —
  `wrap=true` template; no offset constant.
- `src/main/kotlin/com/github/xepozz/ide/introspector/exec/KotlinExecutor.kt:69–103`
  — `Future + Future.cancel(true)` pattern; correct timeout-cancellation
  model the compile path should mirror.
- `src/testScripting/kotlin/.../KotlinCompileOnlyTest.kt` — 7 tests, 3
  ignored.
- `build.gradle.kts:90–103, 150–184` — `testRuntimeClasspath` scripting
  exclusion + `testScripting` source set rationale.
- `docs/plans/exec-compile-check.md` — original contract.
- `CLAUDE.md` — Hard rules (timeouts, classloader policy, exec-tool ethos).
