package com.github.xepozz.ide.introspector.context

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Bm25RankerTest {
    private val ranker = Bm25Ranker()

    private fun entry(
        id: String,
        terms: Map<String, Int>,
        length: Int,
        source: String = "generated",
        title: String = "",
        tags: List<String> = emptyList(),
    ): ManifestEntry = ManifestEntry(
        id = id,
        title = title,
        kind = "doc",
        source = source,
        tags = tags,
        tokenEstimate = length,
        relativePath = "$id.md",
        termFrequencies = terms,
        length = length,
    )

    private fun manifest(
        entries: List<ManifestEntry>,
        documentFrequencies: Map<String, Int>,
        documentCount: Int = entries.size,
        averageDocumentLength: Double = if (entries.isEmpty()) 0.0 else entries.map { it.length }.average(),
    ): Manifest = Manifest(
        header = ManifestHeader(schemaVersion = 1, generatedForBuild = "test", entryCount = entries.size),
        entries = entries,
        corpusStats = CorpusStats(
            documentCount = documentCount,
            averageDocumentLength = averageDocumentLength,
            documentFrequencies = documentFrequencies,
        ),
    )

    @Test
    fun blankQueryReturnsEmptyList() {
        val manifest = manifest(
            entries = listOf(entry("a", mapOf("inspection" to 3), length = 100)),
            documentFrequencies = mapOf("inspection" to 1),
        )
        assertTrue(ranker.rank(manifest, "   ").isEmpty())
        assertTrue(ranker.rank(manifest, "").isEmpty())
    }

    @Test
    fun matchingEntryRanksAboveNonMatchingEntry() {
        val matching = entry("match", mapOf("inspection" to 4), length = 100)
        val nonMatching = entry("other", mapOf("service" to 4), length = 100)
        val manifest = manifest(
            entries = listOf(nonMatching, matching),
            documentFrequencies = mapOf("inspection" to 1, "service" to 1),
        )
        val results = ranker.rank(manifest, "inspection")
        assertEquals("match", results.first().entry.id)
    }

    @Test
    fun higherTermFrequencyYieldsHigherBm25() {
        val low = entry("low", mapOf("inspection" to 1), length = 100)
        val high = entry("high", mapOf("inspection" to 8), length = 100)
        val manifest = manifest(
            entries = listOf(low, high),
            documentFrequencies = mapOf("inspection" to 2),
        )
        val results = ranker.rank(manifest, "inspection")
        val highScore = results.first { it.entry.id == "high" }
        val lowScore = results.first { it.entry.id == "low" }
        assertTrue(highScore.bm25 > lowScore.bm25)
        assertEquals("high", results.first().entry.id)
    }

    @Test
    fun rarerTermOutranksCommonTermAtEqualTermFrequency() {
        val rareMatch = entry("rare", mapOf("inspection" to 3), length = 100)
        val commonMatch = entry("common", mapOf("service" to 3), length = 100)
        val filler = (0 until 2).map { entry("filler$it", mapOf("service" to 1), length = 100) }
        val entries = listOf(rareMatch, commonMatch) + filler
        val manifest = manifest(
            entries = entries,
            documentFrequencies = mapOf("inspection" to 1, "service" to 3),
            documentCount = entries.size,
        )
        val results = ranker.rank(manifest, "inspection service", limit = entries.size)
        val rareScore = results.first { it.entry.id == "rare" }
        val commonScore = results.first { it.entry.id == "common" }
        assertTrue(rareScore.bm25 > commonScore.bm25)
    }

    @Test
    fun manualSourceScoresHigherThanGeneratedWhenOtherwiseEqual() {
        val manual = entry("manual-doc", mapOf("inspection" to 3), length = 100, source = "manual")
        val generated = entry("generated-doc", mapOf("inspection" to 3), length = 100, source = "generated")
        val manifest = manifest(
            entries = listOf(generated, manual),
            documentFrequencies = mapOf("inspection" to 2),
        )
        val results = ranker.rank(manifest, "inspection")
        assertEquals("manual-doc", results.first().entry.id)
        val manualScore = results.first { it.entry.id == "manual-doc" }.score
        val generatedScore = results.first { it.entry.id == "generated-doc" }.score
        assertTrue(manualScore > generatedScore)
    }

    @Test
    fun entriesBelowScoreFloorAreDropped() {
        val matching = entry("match", mapOf("inspection" to 5), length = 100)
        val nonMatching = entry("nomatch", mapOf("unrelated" to 5), length = 100)
        val manifest = manifest(
            entries = listOf(matching, nonMatching),
            documentFrequencies = mapOf("inspection" to 1, "unrelated" to 1),
        )
        val results = ranker.rank(manifest, "inspection")
        assertEquals(1, results.size)
        assertEquals("match", results.first().entry.id)
    }

    @Test
    fun limitCapsResultSize() {
        val entries = (0 until 10).map { entry("e$it", mapOf("inspection" to it + 1), length = 100) }
        val manifest = manifest(
            entries = entries,
            documentFrequencies = mapOf("inspection" to entries.size),
        )
        val results = ranker.rank(manifest, "inspection", limit = 3)
        assertEquals(3, results.size)
    }

    @Test
    fun tiesBrokenByIdAscending() {
        val beta = entry("beta", mapOf("inspection" to 3), length = 100)
        val alpha = entry("alpha", mapOf("inspection" to 3), length = 100)
        val manifest = manifest(
            entries = listOf(beta, alpha),
            documentFrequencies = mapOf("inspection" to 2),
        )
        val results = ranker.rank(manifest, "inspection")
        assertEquals(listOf("alpha", "beta"), results.map { it.entry.id })
    }

    @Test
    fun graphProximityProviderRaisesScore() {
        val target = entry("target", mapOf("inspection" to 3), length = 100)
        val rival = entry("rival", mapOf("inspection" to 3), length = 100)
        val manifest = manifest(
            entries = listOf(target, rival),
            documentFrequencies = mapOf("inspection" to 2),
        )
        val baseline = ranker.rank(manifest, "inspection")
        val baselineTargetScore = baseline.first { it.entry.id == "target" }.score
        val provider = GraphProximityProvider { if (it.id == "target") 1.0 else 0.0 }
        val boosted = ranker.rank(manifest, "inspection", graph = provider)
        val boostedTargetScore = boosted.first { it.entry.id == "target" }.score
        assertTrue(boostedTargetScore > baselineTargetScore)
        assertEquals("target", boosted.first().entry.id)
        assertEquals(1.0, boosted.first { it.entry.id == "target" }.graphScore, 0.0)
    }

    @Test
    fun singleCandidateProducesFiniteScoreInUnitRange() {
        val manifest = manifest(
            entries = listOf(entry("solo", mapOf("inspection" to 3), length = 100)),
            documentFrequencies = mapOf("inspection" to 1),
        )
        val results = ranker.rank(manifest, "inspection")
        assertEquals(1, results.size)
        val score = results.first().score
        assertFalse(score.isNaN())
        assertTrue(score.isFinite())
        assertTrue(score in 0.0..1.0)
    }

    @Test
    fun allEqualBm25SetProducesFiniteScores() {
        val entries = (0 until 4).map { entry("e$it", mapOf("inspection" to 3), length = 100) }
        val manifest = manifest(
            entries = entries,
            documentFrequencies = mapOf("inspection" to entries.size),
        )
        val results = ranker.rank(manifest, "inspection", limit = entries.size)
        assertEquals(entries.size, results.size)
        results.forEach {
            assertFalse(it.score.isNaN())
            assertTrue(it.score in 0.0..1.0)
        }
    }

    @Test
    fun matchedTermsListsOnlyQueryTermsPresentInEntry() {
        val matching = entry("doc", mapOf("inspection" to 2), length = 100)
        val manifest = manifest(
            entries = listOf(matching),
            documentFrequencies = mapOf("inspection" to 1, "service" to 1),
        )
        val results = ranker.rank(manifest, "inspection service")
        assertEquals(listOf("inspection"), results.first().matchedTerms)
    }
}
