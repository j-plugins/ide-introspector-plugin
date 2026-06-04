package com.github.xepozz.ide.introspector.context

data class ScoredEntry(
    val entry: ManifestEntry,
    val score: Double,
    val bm25: Double,
    val titleScore: Double,
    val tagScore: Double,
    val graphScore: Double,
    val matchedTerms: List<String>,
)
