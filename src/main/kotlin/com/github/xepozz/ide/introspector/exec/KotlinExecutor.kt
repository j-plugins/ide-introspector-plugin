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
            // Unwrap InvocationTargetException + chained causes — Method.invoke wraps every
            // failure, often with an inner cause whose message is the real diagnostic.
            val root = generateSequence<Throwable>(t) { it.cause }.lastOrNull() ?: t
            val rootMessage = root.message ?: root.javaClass.simpleName
            return ExecutionResult(false, null, null,
                stdoutBuf.toString().ifEmpty { null },
                stderrBuf.toString().ifEmpty { null },
                "Execution setup failed: ${root.javaClass.simpleName}: $rootMessage\n${root.stackTraceToString().take(3000)}",
                elapsedMs(startNs), warnings)
        } finally {
            if (args.captureStdout) System.setOut(originalOut)
            if (args.captureStderr) System.setErr(originalErr)
            try {
                Disposer.dispose(disposable)
            } catch (throwable: Throwable) {
                log.debug("Disposing exec disposable failed", throwable)
            }
            try {
                tempRoot.toFile().deleteRecursively()
            } catch (throwable: Throwable) {
                log.debug("Cleaning up exec temp dir failed", throwable)
            }
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
        val method = compilerKtClass.declaredMethods.firstOrNull { it.name == "compile" }
            ?: error("EmbeddedCompilerKt exposes no 'compile' method — embedded-compiler wrapper version mismatch")
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

    /**
     * Classloader for `EmbeddedCompilerKt.compile()`. We build a **fresh, isolated**
     * UrlClassLoader over (a) the IDE Kotlin plugin's `kotlinc/lib` jars (full
     * kotlin-compiler + scripting + stdlib) plus (b) our 8-KB wrapper jar plus (c) the
     * JDK class roots. Parent is the JDK bootstrap loader — NOT the IDE's PluginClassLoader.
     *
     * Why isolated and not just `kotlinPlugin.classLoader` as parent: the Kotlin plugin
     * exposes its public API via PluginClassLoader but **does NOT export CLI compiler
     * internals** (`org.jetbrains.kotlin.cli.common.messages.MessageRenderer`, etc.) — they
     * live in the Kotlin plugin's separate `kotlinc/lib/kotlin-compiler.jar` and are
     * loaded only when the plugin internally needs them.
     *
     * Why parent = null and not IDE classloader: kotlin-compiler.jar carries duplicate
     * `com.intellij.openapi.*` classes (un-shaded). Mixing them with the IDE's own
     * com.intellij.* in one classloader chain leads to "class X seen from two loaders"
     * errors and `ApplicationManager.ourApplication` being initialised twice.
     *
     * Trade-off: requires the org.jetbrains.kotlin plugin to be installed. Most IntelliJ
     * IDEs ship it (IDEA, Android Studio, PyCharm Pro, GoLand, WebStorm, RubyMine);
     * DataGrip, Rider, RustRover, CLion don't. Phase 2 adds a Maven-download fallback.
     */
    private val compilerClassLoader: ClassLoader by lazy {
        val wrapperJar = findWrapperJar()
            ?: error("kotlin-compiler-wrapper jar not found in plugin's lib/ — the build is broken.")
        val kotlincJars = kotlincJars()
        check(kotlincJars.isNotEmpty()) {
            "Couldn't find the Kotlin plugin's kotlinc/lib jars in this IDE. " +
                "exec.execute_kotlin_in_ide requires the org.jetbrains.kotlin plugin. " +
                "Install it from Settings → Plugins → Marketplace and restart."
        }
        UrlClassLoader.build()
            .files((listOf(wrapperJar) + kotlincJars).map(File::toPath) + jdkClassRoots())
            .noPreload()
            .allowBootstrapResources()
            .useCache()
            .get()
    }

    private fun findWrapperJar(): File? {
        val ourDescriptor = PluginLookup.findPlugin(PluginId.getId("com.github.xepozz.ide.introspector"))
            ?: return null
        val libDir = ourDescriptor.pluginPath?.toFile()?.resolve("lib") ?: return null
        return libDir.listFiles { f -> f.isFile && f.name.startsWith("kotlin-compiler-wrapper") && f.name.endsWith(".jar") }
            ?.firstOrNull()
    }

    /**
     * The Kotlin IDE plugin ships its full compiler under `<plugin>/kotlinc/lib/`. We pull
     * every jar from there except the `*-sources.jar` files (no need at runtime) and the
     * Kotlin **compiler plugins** (which target IntelliJ APIs and clash with our isolated
     * loader). LivePlugin uses the same filter rule.
     */
    private fun kotlincJars(): List<File> {
        val kotlinPlugin = PluginLookup.findPlugin(PluginId.getId("org.jetbrains.kotlin")) ?: return emptyList()
        val kotlincLib = kotlinPlugin.pluginPath?.toFile()?.resolve("kotlinc/lib") ?: return emptyList()
        if (!kotlincLib.isDirectory) return emptyList()
        return kotlincLib.listFiles { f ->
            f.isFile && f.name.endsWith(".jar") &&
                !f.name.endsWith("-sources.jar") &&
                !f.name.contains("compiler-plugin")
        }?.toList().orEmpty()
    }

    /**
     * Best-effort discovery of JDK class roots that the compiler needs to resolve `java.*`.
     * Tries IntelliJ's `JavaSdkUtil.getJdkClassesRoots` reflectively first, falls back to
     * the `${java.home}/lib/modules` jimage file (works on JDK 9+).
     */
    private fun jdkClassRoots(): List<java.nio.file.Path> {
        val jdkRoot = File(System.getProperty("java.home"))
        try {
            val cls = Class.forName("org.jetbrains.jps.model.java.impl.JavaSdkUtil")
            val m = cls.methods.firstOrNull { it.name == "getJdkClassesRoots" && it.parameterCount == 2 }
            if (m != null) {
                @Suppress("UNCHECKED_CAST")
                return m.invoke(null, jdkRoot.toPath(), true) as List<java.nio.file.Path>
            }
        } catch (throwable: Throwable) {
            log.debug("JavaSdkUtil.getJdkClassesRoots unavailable — falling back to jimage", throwable)
        }
        val jrtModules = jdkRoot.resolve("lib/modules")
        return if (jrtModules.isFile) listOf(jrtModules.toPath()) else emptyList()
    }

    private fun buildCompilerClasspath(): List<File> {
        val out = LinkedHashSet<File>()

        // 1) Every jar in the IDE's main lib/ — com.intellij.* platform classes.
        val ideLib = File(PathManager.getLibPath())
        if (ideLib.isDirectory) {
            ideLib.listFiles { f -> f.isFile && (f.name.endsWith(".jar") || f.name.endsWith(".zip")) }
                ?.let { out.addAll(it) }
        }

        // 2) Our own plugin's lib/ — user code can reference com.github.xepozz.* types.
        val ourDescriptor = PluginLookup.findPlugin(PluginId.getId("com.github.xepozz.ide.introspector"))
        ourDescriptor?.pluginPath?.toFile()?.resolve("lib")?.listFiles { f ->
            f.isFile && f.name.endsWith(".jar")
        }?.let { out.addAll(it) }

        // 3) Kotlin plugin's bundled `kotlinc/lib/kotlin-stdlib*.jar` + `kotlin-reflect.jar` +
        //    coroutines — kotlin.* / kotlin.collections.* symbols at compile time. Skip the
        //    bigger `kotlin-compiler.jar` (156 MB) — it carries duplicate IntelliJ classes
        //    that confuse K2 codegen (the user code's compile classpath should NOT include
        //    the compiler itself).
        val kotlinPlugin = PluginLookup.findPlugin(PluginId.getId("org.jetbrains.kotlin"))
        val kotlinPluginRoot = kotlinPlugin?.pluginPath?.toFile()
        kotlinPluginRoot?.resolve("kotlinc/lib")?.listFiles { f ->
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
