import org.jetbrains.intellij.platform.gradle.TestFrameworkType

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

    // Phase 2: Kotlin runtime execution.
    //
    // We do NOT bundle kotlin-compiler-embeddable (≈57 MB) anymore. Instead, at runtime
    // KotlinExecutor reaches into the IDE's bundled Kotlin plugin's classloader, which
    // already has the full kotlin compiler available (the Kotlin plugin uses it for its
    // own kts-script support). Our 8-KB wrapper jar (the `:kotlin-compiler-wrapper`
    // subproject) is loaded into a child URL classloader whose parent IS the Kotlin
    // plugin's classloader — so K2JVMCompiler resolves there without a duplicate
    // ApplicationEnvironment.
    //
    // Trade-off: exec.execute_kotlin_in_ide requires the user's IDE to ship the
    // org.jetbrains.kotlin plugin. IntelliJ IDEA, Android Studio, PyCharm Pro, GoLand,
    // WebStorm, RubyMine all do; DataGrip, Rider, RustRover, CLion don't. Phase 2 of this
    // plan adds a Maven-Central lazy-download fallback for those IDEs.
    runtimeOnly(project(":kotlin-compiler-wrapper"))

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

val corpusAssembler: Configuration by configurations.creating

dependencies {
    corpusAssembler(project(":corpus-core"))
    corpusAssembler("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
}

val corpusGeneratedForBuild: String =
    providers.gradleProperty("corpus.generatedForBuild").orNull ?: "252.28238.29"

val assembleContextCorpus by tasks.registering(JavaExec::class) {
    val corpusDirectory = layout.projectDirectory.dir("corpus")
    val outputDirectory = layout.buildDirectory.dir("corpus")
    inputs.dir(corpusDirectory).withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.property("generatedForBuild", corpusGeneratedForBuild)
    outputs.dir(outputDirectory)
    classpath = corpusAssembler
    mainClass.set("com.github.xepozz.ide.introspector.context.CorpusAssemblerMainKt")
    args(
        corpusDirectory.asFile.absolutePath,
        outputDirectory.get().asFile.absolutePath,
        corpusGeneratedForBuild,
    )
}

tasks.processResources {
    dependsOn(assembleContextCorpus)
    from(layout.buildDirectory.dir("corpus")) {
        into("context-corpus")
    }
}
