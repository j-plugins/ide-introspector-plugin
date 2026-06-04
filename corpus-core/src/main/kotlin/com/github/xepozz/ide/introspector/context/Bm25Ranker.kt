package com.github.xepozz.ide.introspector.context

import kotlin.math.ln

class Bm25Ranker(
    private val tokenizer: Tokenizer = Tokenizer(),
    private val weights: RankingWeights = RankingWeights.DEFAULT,
    private val k1: Double = 1.2,
    private val b: Double = 0.75,
) {
    fun rank(
        manifest: Manifest,
        rawQuery: String,
        limit: Int = 10,
        graph: GraphProximityProvider = GraphProximityProvider.NONE,
    ): List<ScoredEntry> {
        val queryTerms = tokenizer.tokenize(rawQuery).distinct()
        if (queryTerms.isEmpty()) return emptyList()

        val stats = manifest.corpusStats
        val candidates = manifest.entries.map { entry ->
            Candidate(entry, computeBm25(entry, queryTerms, stats), matchedTerms(entry, queryTerms))
        }

        val maximum = candidates.maxOfOrNull { it.rawBm25 } ?: 0.0

        return candidates
            .map { candidate ->
                val bm25Norm = if (maximum > 0.0) candidate.rawBm25 / maximum else 0.0
                val signals = computeSignals(candidate.entry, queryTerms, bm25Norm, graph)
                ScoredEntry(
                    entry = candidate.entry,
                    score = signals.score,
                    bm25 = candidate.rawBm25,
                    titleScore = signals.titleScore,
                    tagScore = signals.tagScore,
                    graphScore = signals.graphScore,
                    matchedTerms = candidate.matchedTerms,
                )
            }
            .filter { it.score >= weights.scoreFloor }
            .sortedWith(compareByDescending<ScoredEntry> { it.score }.thenBy { it.entry.id })
            .take(limit)
    }

    private fun computeBm25(entry: ManifestEntry, queryTerms: List<String>, stats: CorpusStats): Double {
        val documentLength = entry.length.toDouble()
        val lengthRatio = if (stats.averageDocumentLength > 0.0) documentLength / stats.averageDocumentLength else 0.0
        return queryTerms.sumOf { term ->
            val frequency = entry.termFrequencies[term] ?: 0
            if (frequency == 0) {
                0.0
            } else {
                val documentFrequency = stats.documentFrequencies[term] ?: 0
                val inverseDocumentFrequency = inverseDocumentFrequency(stats.documentCount, documentFrequency)
                val numerator = frequency * (k1 + 1)
                val denominator = frequency + k1 * (1 - b + b * lengthRatio)
                inverseDocumentFrequency * numerator / denominator
            }
        }
    }

    private fun inverseDocumentFrequency(documentCount: Int, documentFrequency: Int): Double =
        ln(1 + (documentCount - documentFrequency + 0.5) / (documentFrequency + 0.5))

    private fun matchedTerms(entry: ManifestEntry, queryTerms: List<String>): List<String> =
        queryTerms.filter { (entry.termFrequencies[it] ?: 0) > 0 }

    private fun computeSignals(
        entry: ManifestEntry,
        queryTerms: List<String>,
        bm25Norm: Double,
        graph: GraphProximityProvider,
    ): Signals {
        val titleScore = fractionPresent(queryTerms, tokenizer.tokenize(entry.title).toSet())
        val tagTokens = entry.tags.flatMap { tokenizer.tokenize(it) }.toSet()
        val tagScore = fractionPresent(queryTerms, tagTokens)
        val manualPriority = if (entry.source == "manual") 1.0 else 0.0
        val graphScore = graph.proximity(entry).coerceIn(0.0, 1.0)
        val weightedSum = weights.bodyBm25 * bm25Norm +
            weights.titleMatch * titleScore +
            weights.tagMatch * tagScore +
            weights.manualPriority * manualPriority +
            weights.graphProximity * graphScore
        val weightTotal = weights.bodyBm25 +
            weights.titleMatch +
            weights.tagMatch +
            weights.manualPriority +
            weights.graphProximity
        val score = if (weightTotal > 0.0) weightedSum / weightTotal else 0.0
        return Signals(score, titleScore, tagScore, graphScore)
    }

    private fun fractionPresent(queryTerms: List<String>, candidateTokens: Set<String>): Double =
        queryTerms.count { it in candidateTokens }.toDouble() / queryTerms.size

    private class Candidate(
        val entry: ManifestEntry,
        val rawBm25: Double,
        val matchedTerms: List<String>,
    )

    private class Signals(
        val score: Double,
        val titleScore: Double,
        val tagScore: Double,
        val graphScore: Double,
    )
}
