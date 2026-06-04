package com.github.xepozz.ide.introspector.context

import java.io.File

private val BUILD_MARKER = Regex("""idea/(\d+\.\d+\.\d+)""")

fun main(arguments: Array<String>) {
    if (arguments.size < 2) {
        System.err.println("usage: SdkDocsConverter <llmsTxtPath> <outputDir> [build]")
        kotlin.system.exitProcess(2)
    }

    val llmsFile = File(arguments[0])
    val outputDirectory = File(arguments[1])
    if (!llmsFile.isFile) {
        System.err.println("llms.txt not found: ${llmsFile.absolutePath}")
        kotlin.system.exitProcess(1)
    }

    val text = llmsFile.readText(Charsets.UTF_8)
    val build = arguments.getOrNull(2)
        ?: BUILD_MARKER.find(text)?.groupValues?.get(1)
        ?: "unknown"

    val documents = SdkDocsConverter(build).convert(text, SdkDocsNarrowCore.TITLES)

    val foundTitles = LlmsTopicSplitter.split(text).map { it.title }.toSet()
    val missing = SdkDocsNarrowCore.TITLES.filterNot { it in foundTitles }
    if (missing.isNotEmpty()) {
        System.err.println("WARNING: ${missing.size} selected topic(s) not found in llms.txt: $missing")
    }

    if (outputDirectory.isDirectory) {
        outputDirectory.listFiles { file -> file.extension == "md" }?.forEach { it.delete() }
    }
    outputDirectory.mkdirs()
    for (document in documents) {
        File(outputDirectory, document.relativePath).writeText(document.content, Charsets.UTF_8)
    }

    println("SDK docs converted: ${documents.size} topics (build $build) -> ${outputDirectory.absolutePath}")
}
