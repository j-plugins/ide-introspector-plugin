import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import java.io.File

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.changelog")
    id("com.google.devtools.ksp")
    // Kover instead of JaCoCo: the IntelliJ Platform Gradle plugin runs tests under
    // com.intellij.util.lang.PathClassLoader, which JaCoCo's on-the-fly instrumentation
    // can't see — every counter ends up 0/N. Kover (also a JetBrains project) handles
    // that classloader natively and emits a JaCoCo-compatible XML alongside its own HTML.
    id("org.jetbrains.kotlinx.kover")
}

kover {
    reports {
        filters {
            includes {
                classes("com.github.xepozz.ide.introspector.*")
            }
            // Generated / IDE-only surfaces — keep them out so the headline number reflects
            // logic we actually own and can test off-IDE.
            excludes {
                classes(
                    "com.github.xepozz.ide.introspector.toolwindow.*",   // Swing tool window
                    "com.github.xepozz.ide.introspector.tools.*",        // MCP toolset entry points
                    "com.github.xepozz.ide.introspector.exec.*",         // Kotlin runtime execution
                    "com.github.xepozz.ide.introspector.model.*",        // @Serializable data classes
                    "com.github.xepozz.ide.introspector.core.ClassSourceResolver*", // Java PSI heavy
                    "*\$\$serializer",                                      // kotlinx.serialization synthetics
                    "*Companion",                                           // boilerplate
                )
            }
        }
    }
}

dependencies {
    // kotlinx-serialization-json is bundled with the IDE (and with the com.intellij.mcpServer
    // plugin). We must use the platform copy at runtime — bundling our own creates two
    // independent KSerializer classloaders, which makes serializerOrNull(KType) return null
    // for our @Serializable data classes when MCP's reflection-based bridge looks them up,
    // and the bridge then throws "Result type X is not serializable".
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")

    // Phase 2: Kotlin runtime execution via embedded K2 compiler — same approach as
    // LivePlugin. We bundle kotlin-compiler-embeddable + scripting jars in the plugin zip,
    // but a post-prepareSandbox task (see below) MOVES them out of `lib/` into a sibling
    // `kotlin-compiler/` folder so the IntelliJ PluginClassLoader never sees them. At
    // runtime our KotlinExecutor builds a dedicated UrlClassLoader over those jars and
    // drives K2JVMCompiler.exec() through reflection — exactly like LivePlugin.
    //
    // `runtimeOnly` keeps these off the test classpath (they bundle older IntelliJ resources
    // that shadow the IDE's modern ones during BasePlatformTestCase). The
    // testRuntimeClasspath excludes below are defensive duplicates of the same intent.
    runtimeOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.3.21")
    runtimeOnly("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:2.3.21")
    runtimeOnly("org.jetbrains.kotlin:kotlin-scripting-jvm:2.3.21")
    runtimeOnly("org.jetbrains.kotlin:kotlin-scripting-common:2.3.21")
    runtimeOnly("org.jetbrains.kotlin:kotlin-stdlib:2.3.21")
    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:2.3.21")
    // Our wrapper subproject — its jar carries EmbeddedCompiler.kt and is relocated
    // alongside the kotlin-* jars into `kotlin-compiler/` post-prepareSandbox.
    runtimeOnly(project(":kotlin-compiler-wrapper"))

    // `@KotlinScript` annotation lives in kotlin-scripting-common; we need it at compile
    // time on our `IntrospectorScript` template class. The annotation class isn't on the
    // runtime classpath after the relocate task — but the JVM doesn't require annotation
    // classes to be loaded for instances to exist (RuntimeRetention only matters when
    // reflection tries to read it). The script compiler reads the annotation via its own
    // classpath in kotlin-compiler/, so no conflict.
    compileOnly("org.jetbrains.kotlin:kotlin-scripting-common:2.3.21")

    // docs/MCP_TOOLS.md generator — runs as part of compileKotlin; see doc-processor/.
    ksp(project(":doc-processor"))

    testImplementation("junit:junit:4.13.2")

    intellijPlatform {
        intellijIdea("2025.2.6.2")
        bundledPlugin("org.jetbrains.kotlin")
        bundledPlugin("com.intellij.java")
        plugin("com.intellij.mcpServer", "252.28238.29")
        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.Plugin.Java)
    }
}

// `runtimeOnly` puts the deps on production runtime AND testRuntimeClasspath (the latter
// extends the former). kotlin-compiler-embeddable bundles an older
// `messages/JavaPsiBundle.properties` (and other IntelliJ platform resources) that shadow
// the IDE's modern ones during tests, triggering missing-resource errors during the
// platform's FileTypeManager preload. Removing the scripting stack from test runtime is
// safe because tests don't exec Kotlin scripts.
configurations.testRuntimeClasspath {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-compiler-embeddable")
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-scripting-compiler-embeddable")
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-scripting-compiler-impl-embeddable")
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-scripting-jvm")
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-scripting-common")
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-script-runtime")
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-daemon-embeddable")
}

// Move kotlin-compiler-embeddable + scripting + kotlin-stdlib/reflect + our wrapper jar
// from `<sandbox>/plugins/ide-introspector/lib/` into a sibling `kotlin-compiler/` after
// prepareSandbox. The IntelliJ PluginClassLoader only walks `lib/` — relocating these
// keeps them OFF the plugin classpath, so there are no kotlin class duplicates between
// our bundled compiler and the IDE's bundled Kotlin plugin. KotlinExecutor reads them via
// `pluginPath/kotlin-compiler/` at runtime into a dedicated UrlClassLoader.
//
// Same approach as LivePlugin's "Move kotlin compiler jars from plugin classpath into a
// separate folder so that there are no conflicts" task in its build.gradle.
// The path is computed inside doLast purely with java.io.File ops (no Project script
// references) but Gradle's configuration cache still flags closure capture. Opting out
// for this task only is the pragmatic move — relocation is fast enough that recomputing
// on every build is not a problem.
val sandboxRootPath: String = layout.projectDirectory.dir(".intellijPlatform/sandbox").asFile.absolutePath
val relocateKotlinCompilerJars = tasks.register("relocateKotlinCompilerJars") {
    notCompatibleWithConfigurationCache("Walks live sandbox plugin tree")
    dependsOn("prepareSandbox")
    doLast {
        val root = File(sandboxRootPath)
        val pluginDir: File = root.takeIf { it.isDirectory }
            ?.walkTopDown()
            ?.firstOrNull { it.name == "ide-introspector" && it.parentFile?.name == "plugins" }
            ?: error("relocateKotlinCompilerJars: no sandbox plugin dir found under $sandboxRootPath")
        val libDir = pluginDir.resolve("lib")
        val targetDir = pluginDir.resolve("kotlin-compiler").apply { mkdirs() }
        val relocatablePrefixes = listOf(
            "kotlin-compiler-embeddable",
            "kotlin-scripting-compiler",
            "kotlin-scripting-common",
            "kotlin-scripting-jvm",
            "kotlin-daemon-embeddable",
            "kotlin-script-runtime",
            "kotlin-stdlib",
            "kotlin-reflect",
            "kotlinx-coroutines",
            "kotlin-compiler-wrapper",
            // org.jetbrains.annotations — required by Kotlin compiler's AnnotationCodegen
            // to emit @Nullable on generated bytecode; bundled as a Kotlin compiler
            // transitive but must live next to the compiler, NOT in plugin classpath.
            "annotations-",
        )
        val moved = mutableListOf<String>()
        libDir.listFiles()?.forEach { f ->
            if (f.isFile && f.name.endsWith(".jar") && relocatablePrefixes.any { f.name.startsWith(it) }) {
                val dest = targetDir.resolve(f.name)
                if (dest.exists()) dest.delete()
                check(f.renameTo(dest)) { "Failed to move ${f.name} to kotlin-compiler/" }
                moved += f.name
            }
        }
        logger.lifecycle("Relocated ${moved.size} kotlin-* jars to ${targetDir.relativeTo(pluginDir)}/: ${moved.joinToString()}")
    }
}
tasks.named("prepareSandbox").configure { finalizedBy(relocateKotlinCompilerJars) }

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild.set("252")
        }
    }
}

kotlin {
    jvmToolchain(21)
}

// Tests should be deterministic across machines: enforce headless mode so a misconfigured
// local display doesn't change behaviour. Window-dependent tests use Assume.assumeFalse(
// GraphicsEnvironment.isHeadless()) and skip cleanly when this is set.
tasks.withType<Test>().configureEach {
    systemProperty("java.awt.headless", "true")
}

// Tell the KSP processor where to write the markdown reference. KSP runs as part of
// compileKotlin so every `./gradlew build` (and `./gradlew buildPlugin`) refreshes the file.
ksp {
    arg("docOutput", layout.projectDirectory.file("docs/MCP_TOOLS.md").asFile.absolutePath)
}
