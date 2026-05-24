plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    // The K2 compiler + scripting infrastructure used by EmbeddedCompiler.compile().
    // `implementation` here so the symbols resolve at compile time inside this subproject
    // ONLY. The main `:` project never depends on these — it loads this jar (and the
    // bundled kotlin-* jars) into a separate UrlClassLoader at runtime.
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.3.21")
    implementation("org.jetbrains.kotlin:kotlin-scripting-common:2.3.21")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:2.3.21")
    implementation("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:2.3.21")
}

kotlin {
    jvmToolchain(21)
}
