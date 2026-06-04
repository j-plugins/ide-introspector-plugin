plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    // `compileOnly` — these resolve K2JVMCompiler / MessageCollector / Services symbols at
    // compile time but are NOT carried into the main plugin's runtime classpath. At runtime
    // this jar (only ~8 KB) is loaded into a UrlClassLoader whose parent is the IDE's
    // Kotlin-plugin classloader, which provides all of these classes via its own jars in
    // <Kotlin plugin>/kotlinc/lib/. Avoids bundling 57 MB of kotlin-compiler-embeddable in
    // the plugin distribution.
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.4.0")
    compileOnly("org.jetbrains.kotlin:kotlin-scripting-common:2.4.0")
    compileOnly("org.jetbrains.kotlin:kotlin-scripting-jvm:2.4.0")
    compileOnly("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:2.4.0")
}

kotlin {
    jvmToolchain(21)
}
