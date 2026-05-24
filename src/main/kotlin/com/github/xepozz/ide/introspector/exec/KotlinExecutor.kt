package com.github.xepozz.ide.introspector.exec

import com.github.xepozz.ide.introspector.core.PluginLookup
import com.github.xepozz.ide.introspector.model.args.ExecuteKotlinArgs
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.util.lang.UrlClassLoader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.swing.SwingUtilities

/**
 * Compiles + executes user-supplied Kotlin inside the running IDE.
 *
 * Architecture (mirrors LivePlugin):
 *   1. [CodeWrapper] turns the user's code into a regular `.kt` file declaring
 *      `class Plugin { fun run(project, disposable): Any? }`.
 *   2. The embedded Kotlin compiler (kotlin-compiler-embeddable + scripting deps,
 *      bundled in `<pluginPath>/kotlin-compiler/`) is loaded into a **dedicated
 *      UrlClassLoader** whose parent is the JDK bootstrap, NOT our PluginClassLoader.
 *      This isolation is essential: kotlin-compiler-embeddable ships duplicates of many
 *      IntelliJ classes; loading them into the IDE's plugin loader corrupts the IDE.
 *   3. Reflectively invoke `EmbeddedCompilerKt.compile(srcDir, classpath, jrePath,
 *      outputDir, anyClass)` to produce `Plugin.class` in a per-call temp dir.
 *   4. Load `Plugin.class` via a child `URLClassLoader` whose parent IS our
 *      PluginClassLoader (so user code sees IntelliJ Platform API + our plugin types).
 *   5. Invoke `Plugin().run(project, disposable)` via reflection; the return value is
 *      serialised to JSON. Bound `Disposable` is disposed afterwards.
 *
 * The 10-second hard timeout from CLAUDE.md applies to both compile and run combined.
 */
object KotlinExecutor {

    private val log = Logger.getInstance(KotlinExecutor::class.java)
    private val executor = Executors.newCachedThreadPool { r ->
        Thread(r, "ide-introspector-exec").apply { isDaemon = true }
    }

    data class ExecutionResult(
        val ok: Boolean,
        val result: kotlinx.serialization.json.JsonElement?,
        val resultPreview: String?,
        val stdout: String?,
        val stderr: String?,
        val errorMessage: String?,
        val durationMs: Long,
        val warnings: List<String>,
    )

    fun execute(args: ExecuteKotlinArgs, project: Project): ExecutionResult {
        val startNs = System.nanoTime()
        val warnings = mutableListOf<String>()
        val effectiveTimeoutMs = effectiveTimeout(args, warnings)

        val disposable = Disposer.newDisposable("ide-introspector-exec-${System.nanoTime()}")
        val stdoutBuf = ByteArrayOutputStream()
        val stderrBuf = ByteArrayOutputStream()
        val originalOut = System.out
        val originalErr = System.err
        if (args.captureStdout) System.setOut(PrintStream(TeeOutputStream(stdoutBuf, originalOut), true))
        if (args.captureStderr) System.setErr(PrintStream(TeeOutputStream(stderrBuf, originalErr), true))

        val tempRoot = Files.createTempDirectory("ide-introspector-exec-")
        val srcDir = tempRoot.resolve("src").apply { Files.createDirectories(this) }
        val outDir = tempRoot.resolve("out").apply { Files.createDirectories(this) }

        try {
            val srcFile = srcDir.resolve("${CodeWrapper.GENERATED_CLASS}.kt")
            Files.writeString(srcFile, CodeWrapper.wrap(args.code))

            val compileErrors = invokeEmbeddedCompiler(srcDir.toFile(), outDir.toFile())
            if (compileErrors.isNotEmpty()) {
                return ExecutionResult(
                    ok = false,
                    result = null,
                    resultPreview = null,
                    stdout = stdoutBuf.toString().ifEmpty { null },
                    stderr = stderrBuf.toString().ifEmpty { null },
                    errorMessage = "Compilation failed:\n" + compileErrors.joinToString("\n").take(16000),
                    durationMs = elapsedMs(startNs),
                    warnings = warnings,
                )
            }

            val future: Future<Any?> = executor.submit(Callable {
                runOn(args.runOn) {
                    val runtimeCl = URLClassLoader(
                        arrayOf(outDir.toUri().toURL()),
                        KotlinExecutor::class.java.classLoader,
                    )
                    val cls = runtimeCl.loadClass("${CodeWrapper.GENERATED_PACKAGE}.${CodeWrapper.GENERATED_CLASS}")
                    val instance = cls.getDeclaredConstructor().newInstance()
                    val runMethod = cls.getDeclaredMethod(
                        "run",
                        Project::class.java,
                        com.intellij.openapi.Disposable::class.java,
                    )
                    runMethod.invoke(instance, project, disposable)
                }
            })

            return try {
                val value: Any? = future.get(effectiveTimeoutMs, TimeUnit.MILLISECONDS)
                val serialised = ResultSerializer.toJson(value)
                ExecutionResult(
                    ok = true,
                    result = serialised.json,
                    resultPreview = value?.toString()?.take(500),
                    stdout = stdoutBuf.toString().ifEmpty { null },
                    stderr = stderrBuf.toString().ifEmpty { null },
                    errorMessage = null,
                    durationMs = elapsedMs(startNs),
                    warnings = warnings + serialised.warnings,
                )
            } catch (t: TimeoutException) {
                future.cancel(true)
                ExecutionResult(false, null, null,
                    stdoutBuf.toString().ifEmpty { null },
                    stderrBuf.toString().ifEmpty { null },
                    "Execution timeout after $effectiveTimeoutMs ms",
                    elapsedMs(startNs), warnings)
            } catch (t: Throwable) {
                val cause = t.cause ?: t
                ExecutionResult(false, null, null,
                    stdoutBuf.toString().ifEmpty { null },
                    stderrBuf.toString().ifEmpty { null },
                    "${cause.javaClass.simpleName}: ${cause.message}\n${cause.stackTraceToString().take(2000)}",
                    elapsedMs(startNs), warnings)
            }
        } catch (t: Throwable) {
            return ExecutionResult(false, null, null,
                stdoutBuf.toString().ifEmpty { null },
                stderrBuf.toString().ifEmpty { null },
                "Execution setup failed: ${t.javaClass.simpleName}: ${t.message}",
                elapsedMs(startNs), warnings)
        } finally {
            if (args.captureStdout) System.setOut(originalOut)
            if (args.captureStderr) System.setErr(originalErr)
            try { Disposer.dispose(disposable) } catch (_: Throwable) {}
            try { tempRoot.toFile().deleteRecursively() } catch (_: Throwable) {}
        }
    }

    // -----------------------------------------------------------------------
    // Embedded-compiler reflection bridge
    // -----------------------------------------------------------------------

    /**
     * Returns compile errors (empty list = success). Throws on classloader / reflection
     * setup failures (treated as compile failures by the caller via the surrounding catch).
     */
    private fun invokeEmbeddedCompiler(srcDir: File, outDir: File): List<String> {
        val classpath = buildCompilerClasspath()
        val compilerKtClass = compilerClassLoader.loadClass(
            "com.github.xepozz.ide.introspector.exec.wrapper.EmbeddedCompilerKt"
        )
        val method = compilerKtClass.declaredMethods.single { it.name == "compile" }
        @Suppress("UNCHECKED_CAST")
        return method.invoke(
            null,
            srcDir.absolutePath,
            classpath,
            File(System.getProperty("java.home")),
            outDir,
            Any::class.java,  // scriptTemplateClass — irrelevant for .kt files but EmbeddedCompiler's signature requires it
        ) as List<String>
    }

    /** Isolated classloader for the embedded Kotlin compiler — must NOT chain into the IDE. */
    private val compilerClassLoader: ClassLoader by lazy {
        val jars = pluginKotlinCompilerJars()
        check(jars.isNotEmpty()) {
            "No kotlin-compiler jars found in <plugin>/kotlin-compiler — was the prepareSandbox relocate task run?"
        }
        UrlClassLoader.build()
            .files(jdkClassRoots() + jars.map(File::toPath))
            .noPreload()
            .allowBootstrapResources()
            .useCache()
            .get()
    }

    private fun pluginKotlinCompilerJars(): List<File> {
        val ourDescriptor = PluginLookup.findPlugin(PluginId.getId("com.github.xepozz.ide.introspector"))
            ?: return emptyList()
        val dir = ourDescriptor.pluginPath?.toFile()?.resolve("kotlin-compiler") ?: return emptyList()
        if (!dir.isDirectory) return emptyList()
        return dir.listFiles { f -> f.isFile && f.name.endsWith(".jar") }?.toList().orEmpty()
    }

    /**
     * Best-effort discovery of JDK class roots (rt.jar / java.base modules) that the
     * embedded compiler needs to resolve `java.*` types. Falls back to scanning
     * `${java.home}/lib/modules` and a few well-known directories.
     */
    private fun jdkClassRoots(): List<Path> {
        val jdkRoot = File(System.getProperty("java.home"))
        // Try IntelliJ's JavaSdkUtil reflectively — present in jps-model.jar in modern IDEs.
        try {
            val cls = Class.forName("org.jetbrains.jps.model.java.impl.JavaSdkUtil")
            val m = cls.methods.firstOrNull { it.name == "getJdkClassesRoots" && it.parameterCount == 2 }
            if (m != null) {
                @Suppress("UNCHECKED_CAST")
                return m.invoke(null, jdkRoot.toPath(), true) as List<Path>
            }
        } catch (_: Throwable) { /* fall through */ }
        // Fallback: just the jrt-fs JDK image — works on JDK 9+.
        val jrtModules = jdkRoot.resolve("lib/modules")
        return if (jrtModules.isFile) listOf(jrtModules.toPath()) else emptyList()
    }

    private fun buildCompilerClasspath(): List<File> {
        val out = LinkedHashSet<File>()

        // 1) Every jar in the IDE's main lib/ — gives us com.intellij.* platform classes.
        val ideLib = File(PathManager.getLibPath())
        if (ideLib.isDirectory) {
            ideLib.listFiles { f -> f.isFile && (f.name.endsWith(".jar") || f.name.endsWith(".zip")) }
                ?.let { out.addAll(it) }
        }

        // 2) Our own plugin's lib/ — so user code can reference com.github.xepozz.* types.
        val ourDescriptor = PluginLookup.findPlugin(PluginId.getId("com.github.xepozz.ide.introspector"))
        ourDescriptor?.pluginPath?.toFile()?.resolve("lib")?.listFiles { f ->
            f.isFile && f.name.endsWith(".jar")
        }?.let { out.addAll(it) }

        // 3) kotlin-stdlib + kotlin-reflect from OUR isolated kotlin-compiler/ — provides
        //    kotlin.* / kotlin.collections.* symbols at compile time. We deliberately do
        //    NOT pull in the IDE Kotlin plugin's kotlinc/lib/kotlin-compiler.jar — that's
        //    a 156 MB "fat" jar with duplicate copies of many compiler-internal classes
        //    that confuse K2's codegen ("Exception while generating code for run …").
        //    At runtime the JVM resolves kotlin.* via the IDE classloader chain (Kotlin
        //    plugin's classloader), which sees its own bundled stdlib — versions match in
        //    practice because we pin our bundled stdlib (2.3.21) close to the IDE's.
        ourDescriptor?.pluginPath?.toFile()?.resolve("kotlin-compiler")?.listFiles { f ->
            f.isFile && f.name.endsWith(".jar") && (
                f.name.startsWith("kotlin-stdlib") ||
                f.name.startsWith("kotlin-reflect") ||
                f.name.startsWith("kotlinx-coroutines")
            )
        }?.let { out.addAll(it) }

        return out.toList()
    }

    // -----------------------------------------------------------------------
    // Misc plumbing
    // -----------------------------------------------------------------------

    private fun effectiveTimeout(args: ExecuteKotlinArgs, warnings: MutableList<String>): Long {
        val s = ExecSettings.getInstance()
        val requested = args.timeoutMs.coerceAtLeast(1)
        if (requested > s.maxTimeoutMs) {
            warnings.add("Requested timeoutMs=$requested exceeds maxTimeoutMs=${s.maxTimeoutMs}; clamped.")
            return s.maxTimeoutMs
        }
        return requested
    }

    private fun <T> runOn(runOn: String, block: () -> T): T = when (runOn) {
        "edt" -> {
            if (SwingUtilities.isEventDispatchThread()) {
                block()
            } else {
                val out = java.util.concurrent.atomic.AtomicReference<Result<T>>()
                ApplicationManager.getApplication().invokeAndWait { out.set(runCatching(block)) }
                requireNotNull(out.get()) { "invokeAndWait returned without populating the result reference" }
                    .getOrThrow()
            }
        }
        else -> block()
    }

    private fun elapsedMs(startNs: Long) = (System.nanoTime() - startNs) / 1_000_000
}

private class TeeOutputStream(
    private val a: java.io.OutputStream,
    private val b: java.io.OutputStream,
) : java.io.OutputStream() {
    override fun write(b1: Int) { a.write(b1); b.write(b1) }
    override fun write(buf: ByteArray, off: Int, len: Int) { a.write(buf, off, len); b.write(buf, off, len) }
    override fun flush() { a.flush(); b.flush() }
}
