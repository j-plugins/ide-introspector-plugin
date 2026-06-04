package com.github.xepozz.ide.introspector.context

import kotlinx.serialization.Serializable

@Serializable
data class Manifest(
    val header: ManifestHeader,
    val entries: List<ManifestEntry>,
    val corpusStats: CorpusStats,
)

@Serializable
data class ManifestHeader(
    val schemaVersion: Int,
    val generatedForBuild: String,
    val entryCount: Int,
)

@Serializable
data class ManifestEntry(
    val id: String,
    val title: String,
    val kind: String,
    val source: String,
    val tags: List<String>,
    val tokenEstimate: Int,
    val relativePath: String,
    val termFrequencies: Map<String, Int>,
    val length: Int,
)

@Serializable
data class CorpusStats(
    val documentCount: Int,
    val averageDocumentLength: Double,
    val documentFrequencies: Map<String, Int>,
)
