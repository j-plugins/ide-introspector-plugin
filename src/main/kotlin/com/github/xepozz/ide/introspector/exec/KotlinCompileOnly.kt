package com.github.xepozz.ide.introspector.exec

import com.github.xepozz.ide.introspector.model.CompileCheckResponse
import com.github.xepozz.ide.introspector.model.CompileDiagnostic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.JvmScriptCompiler
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

/**
 * Minimal [KotlinScript]-annotated template the modular scripting host requires for
 * `createJvmCompilationConfigurationFromTemplate<…>`. We don't use any of the template's
 * own behaviour — the annotation is just the configuration-discovery hook.
 */
@KotlinScript(fileExtension = "compile-check.kts")
abstract class CompileCheckScriptTemplate

/**
 * Compiles a Kotlin snippet via the modular Kotlin scripting host API
 * ([JvmScriptCompiler.invoke] + [ResultWithDiagnostics]), without executing the result.
 *
 * Why not JSR-223? JSR-223's `Compilable.compile` collapses every compile failure into a
 * single `ScriptException` and drops warnings on the floor, which doesn't match the
 * tool's `exec.compile_check` contract (multi-entry diagnostics, FATAL/ERROR/WARNING/INFO,
 * line/col, factory id). [JvmScriptCompiler] returns `ResultWithDiagnostics<CompiledScript>`
 * whose `reports: List<ScriptDiagnostic>` preserves every entry across both Success and
 * Failure outcomes.
 *
 * Sibling of [KotlinExecutor]; deliberately not part of it so the run-loop in
 * `exec.execute_kotlin_in_ide` stays focused on execution and we don't accidentally
 * couple compile-only behaviour to the confirmation / AST / audit flow.
 *
 * Hard 10s timeout — the same cap [KotlinExecutor] enforces (see CLAUDE.md "Timeouts").
 * Implementation uses the same `Future + Future.cancel(true)` pattern as [KotlinExecutor]
 * so a runaway compile actually gets interrupted on timeout (a bare `withTimeoutOrNull`
 * over a non-suspending compile body leaks the thread until the work finishes).
 *
 * Cold-start latency is ~2-3s (loading `kotlin-compiler-embeddable`), warm ~150-400ms;
 * the [compiler] and [compilationConfig] are cached lazily so subsequent calls amortise.
 */
object KotlinCompileOnly {

    private const val TIMEOUT_MS = 10_000L
    private const val SCRIPT_NAME = "compile-check.kts"

    /** Cached across calls — loading the scripting host is the ~2-3 s cold-start cost. */
    private val compiler: JvmScriptCompiler by lazy { JvmScriptCompiler() }

    /**
     * Cached across calls. Built once with `wholeClasspath = true` so snippets see the
     * same classpath the plugin sees (matches what [KotlinExecutor]'s engine resolves).
     */
    private val compilationConfig: ScriptCompilationConfiguration by lazy {
        createJvmCompilationConfigurationFromTemplate<CompileCheckScriptTemplate> {
            jvm {
                dependenciesFromClassContext(
                    contextClass = KotlinCompileOnly::class,
                    wholeClasspath = true,
                )
            }
        }
    }

    /**
     * Daemon executor used for the blocking compile call. `cancel(true)` translates
     * a timeout into `Thread.interrupt()` so we don't leak a worker thread on runaway
     * compiles — the bare-`withTimeoutOrNull` approach does NOT cancel non-suspending
     * compiler work.
     */
    private val executor = Executors.newCachedThreadPool { r ->
        Thread(r, "ide-introspector-compile-check").apply { isDaemon = true }
    }

    suspend fun check(
        code: String,
        wrap: Boolean,
        timeoutMs: Long = TIMEOUT_MS,
    ): CompileCheckResponse = check(code, wrap, timeoutMs, ::doCompile)

    /**
     * Test seam: lets unit tests inject a fake "compile" lambda. Public (not internal) so
     * `KotlinCompileOnlyTest` in the `testScripting` source set — which compiles as a
     * separate Kotlin module — can reach it.
     */
    suspend fun check(
        code: String,
        wrap: Boolean,
        timeoutMs: Long,
        compileFn: (String) -> CompileOutcome,
    ): CompileCheckResponse {
        val startNs = System.nanoTime()
        val source = if (wrap) CodeWrapper.wrap(code) else code

        val future: Future<CompileOutcome> = executor.submit(Callable { compileFn(source) })

        val outcome: CompileOutcome = try {
            withContext(Dispatchers.IO) {
                future.get(timeoutMs, TimeUnit.MILLISECONDS)
            }
        } catch (_: TimeoutException) {
            future.cancel(true)
            return CompileCheckResponse(
                ok = false,
                diagnostics = emptyList(),
                warnings = listOf("Compile timed out after $timeoutMs ms"),
                durationMs = elapsedMs(startNs),
            )
        } catch (t: Throwable) {
            // `future.get` wraps the Callable's exception in ExecutionException — unwrap.
            val cause = t.cause ?: t
            return CompileCheckResponse(
                ok = false,
                diagnostics = listOf(
                    CompileDiagnostic(
                        severity = "FATAL",
                        message = "Compiler threw ${cause.javaClass.simpleName}: ${cause.message ?: "(no message)"}",
                    )
                ),
                warnings = emptyList(),
                durationMs = elapsedMs(startNs),
            )
        }

        return CompileCheckResponse(
            ok = outcome.ok,
            diagnostics = outcome.diagnostics,
            warnings = emptyList(),
            durationMs = elapsedMs(startNs),
        )
    }

    /**
     * Pair-style return so the test seam can fake a multi-diagnostic outcome without
     * dragging the scripting types into the test signature. Public for the same reason
     * the 4-arg `check(...)` overload is — `testScripting` is a separate Kotlin module.
     */
    data class CompileOutcome(val ok: Boolean, val diagnostics: List<CompileDiagnostic>)

    /**
     * The real compile path. Bridges the suspend [JvmScriptCompiler.invoke] back to
     * blocking via `runBlocking` — the surrounding [check] already runs this on a
     * background executor with a `Future.cancel(true)` timeout, so `runBlocking` here
     * is safe and the `Thread.interrupt()` propagates into the compiler.
     *
     * `result.reports` is iterated WHETHER the compile succeeded or failed; warnings on
     * a successful compile are preserved (the whole point of moving off JSR-223).
     */
    private fun doCompile(source: String): CompileOutcome {
        val scriptSource = source.toScriptSource(name = SCRIPT_NAME)
        val result: ResultWithDiagnostics<*> = runBlocking {
            compiler.invoke(scriptSource, compilationConfig)
        }
        val diagnostics = result.reports
            // Drop DEBUG-level noise — anything WARNING/INFO/ERROR/FATAL surfaces.
            .filter { it.severity.ordinal >= ScriptDiagnostic.Severity.INFO.ordinal }
            .map { d ->
                CompileDiagnostic(
                    severity = d.severity.name,
                    line = d.location?.start?.line,
                    column = d.location?.start?.col,
                    file = d.sourcePath ?: SCRIPT_NAME,
                    message = d.message,
                    // ScriptDiagnostic.code is an Int (0 == "no code"); surface as String
                    // so the response shape stays uniform with the rest of the model.
                    factoryId = d.code.takeIf { it != 0 }?.toString(),
                )
            }
        return CompileOutcome(ok = result is ResultWithDiagnostics.Success, diagnostics = diagnostics)
    }

    private fun elapsedMs(startNs: Long) = (System.nanoTime() - startNs) / 1_000_000
}
