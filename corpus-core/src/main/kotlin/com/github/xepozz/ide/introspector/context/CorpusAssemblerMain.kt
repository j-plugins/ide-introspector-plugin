package com.github.xepozz.ide.introspector.context

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

private val MANIFEST_JSON = Json { prettyPrint = true }

fun main(arguments: Array<String>) {
    if (arguments.size < 3) {
        System.err.println("usage: CorpusAssembler <corpusDir> <outputDir> <generatedForBuild>")
        kotlin.system.exitProcess(2)
    }

    val corpusDirectory = File(arguments[0])
    val outputDirectory = File(arguments[1])
    val generatedForBuild = arguments[2]

    val loaded = CorpusLoader(DirectoryFileSource(corpusDirectory)).load()
    val merged = CorpusMerger.merge(loaded.entries)
    val issues = loaded.issues + merged.issues

    printIssues(issues)
    val errors = issues.filter { it.severity == Severity.ERROR }
    if (errors.isNotEmpty()) {
        System.err.println("Corpus assembly failed with ${errors.size} error(s).")
        kotlin.system.exitProcess(1)
    }

    val manifest = ManifestBuilder().build(merged.entries, generatedForBuild)
    writeCorpus(outputDirectory, merged.entries, manifest)
    println("Corpus assembled: ${merged.entries.size} entries -> ${outputDirectory.absolutePath}")
}

private fun printIssues(issues: List<ValidationIssue>) {
    for (issue in issues.sortedWith(compareBy({ it.sourcePath ?: "" }, { it.code.name }))) {
        val location = issue.sourcePath ?: "<corpus>"
        val keySuffix = issue.key?.let { " ($it)" } ?: ""
        println("[${issue.severity}] $location [${issue.code}]$keySuffix ${issue.message}")
    }
}

private fun writeCorpus(outputDirectory: File, entries: List<CorpusEntry>, manifest: Manifest) {
    outputDirectory.mkdirs()
    for (entry in entries) {
        val target = File(outputDirectory, entry.relativePath)
        target.parentFile?.mkdirs()
        target.writeText(entry.body, Charsets.UTF_8)
    }
    File(outputDirectory, "manifest.json").writeText(MANIFEST_JSON.encodeToString(manifest), Charsets.UTF_8)
}
