package com.github.xepozz.ide.introspector.exec.wrapper

import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer.PLAIN_FULL_PATHS
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.text.Charsets.UTF_8

/**
 * Drives the embedded Kotlin compiler (K2JVMCompiler) to compile every `.kt`/`.kts` file
 * under [sourceDir] into [outputDirectory], using [classpath] as `-cp` and treating
 * [scriptTemplateClass] as the kotlin.scripting compiler-plugin script template.
 *
 * Returns the list of compiler error messages — empty on success.
 *
 * **Loaded via reflection from a dedicated UrlClassLoader** that is NOT a child of the
 * IntelliJ PluginClassLoader. Arguments passed across that classloader boundary therefore
 * MUST use only `java.*` types (List<String>, List<File>, File, Class<*>); pure Kotlin
 * types (collections from kotlin-stdlib loaded by the IDE classloader) would be incompatible
 * with the compiler's view of kotlin.collections.* loaded by our isolated kotlin-stdlib.
 *
 * Mirrors LivePlugin's `kotlin-compiler-wrapper/src/liveplugin/implementation/kotlin/EmbeddedCompiler.kt`.
 */
fun compile(
    sourceDir: String,
    classpath: List<File>,
    jrePath: File,
    outputDirectory: File,
    scriptTemplateClass: Class<*>,
): List<String> {
    val sourceFiles = File(sourceDir).walkTopDown()
        .filter { it.isFile && (it.extension == "kt" || it.extension == "kts") }
        .map { it.absolutePath }
        .toList()

    val args = K2JVMCompilerArguments().apply {
        freeArgs = sourceFiles
        this.classpath = classpath.joinToString(File.pathSeparator) { it.absolutePath }
        destination = outputDirectory.absolutePath
        jdkHome = jrePath.absolutePath
        moduleName = "IdeIntrospectorEmbeddedCompiler"
        noStdlib = true
        reportOutputFiles = false
        languageVersion = "2.3"
        apiVersion = "2.3"
        jvmTarget = "21"
        pluginOptions = arrayOf(
            "plugin:kotlin.scripting:script-templates=${scriptTemplateClass.name}"
        )
        allowAnyScriptsInSourceRoots = true
        useFirLT = false
        // K2 (Kotlin 2.3) emits lambdas via invokedynamic + LambdaMetafactory by default,
        // and JvmIrCodegen crashes ("Exception while generating code for ...") for inline
        // lambdas that capture a nullable receiver dereferenced with `!!`. Forcing the
        // legacy class-per-lambda lowering sidesteps the bug at zero runtime cost.
        lambdas = "class"
        pluginClasspaths = classpath
            .filter { it.name.contains("scripting-compiler") }
            .map { it.absolutePath }
            .toTypedArray()
    }

    val messageCollector = ErrorMessageCollector()
    val capturedErr = ByteArrayOutputStream()
    val originalErr = System.err
    System.setErr(java.io.PrintStream(capturedErr, true))
    val exitCode = try {
        K2JVMCompiler().exec(messageCollector, Services.EMPTY, args)
    } finally {
        System.setErr(originalErr)
    }
    return if (exitCode == ExitCode.OK) {
        emptyList()
    } else {
        val text = buildString {
            append(messageCollector.errStream.toString())
            if (capturedErr.size() > 0) {
                append("\n--- compiler stderr ---\n")
                append(capturedErr.toString())
            }
        }.ifEmpty { "Compiler finished with exit code $exitCode but no errors were reported." }
        text.lines()
    }
}

private class ErrorMessageCollector(
    val errStream: ByteArrayOutputStream = ByteArrayOutputStream(),
    private val messageRenderer: MessageRenderer = PLAIN_FULL_PATHS,
) : MessageCollector {
    private var hasErrors = false

    override fun report(
        severity: CompilerMessageSeverity,
        message: String,
        location: CompilerMessageSourceLocation?,
    ) {
        if (severity in CompilerMessageSeverity.VERBOSE) return
        if (severity.isError) {
            errStream.write((messageRenderer.render(severity, message, location) + "\n").toByteArray(UTF_8))
            hasErrors = true
        }
    }

    override fun clear() {}

    override fun hasErrors() = hasErrors
}
