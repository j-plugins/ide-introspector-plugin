package com.github.xepozz.ide.introspector.context

import java.util.Locale

class ManifestBuilder(
    private val tokenizer: Tokenizer = Tokenizer(),
    private val termMapBuilder: TermMapBuilder = TermMapBuilder(),
    private val schemaVersion: Int = 1,
) {
    fun build(entries: List<CorpusEntry>, generatedForBuild: String): Manifest {
        val sortedEntries = entries.sortedBy { it.id }
        val tokensByEntry = sortedEntries.associateWith { tokenizer.tokenize(it.body) }

        val manifestEntries = sortedEntries.map { entry ->
            buildManifestEntry(entry, tokensByEntry.getValue(entry))
        }

        val header = ManifestHeader(
            schemaVersion = schemaVersion,
            generatedForBuild = generatedForBuild,
            entryCount = entries.size,
        )

        return Manifest(
            header = header,
            entries = manifestEntries,
            corpusStats = buildCorpusStats(manifestEntries, tokensByEntry.values),
        )
    }

    private fun buildManifestEntry(entry: CorpusEntry, tokens: List<String>): ManifestEntry =
        ManifestEntry(
            id = entry.id,
            title = entry.frontmatter.title ?: entry.id,
            kind = (entry.frontmatter.kind?.name ?: Kind.REFERENCE.name).lowercase(Locale.ROOT),
            source = entry.source.name.lowercase(Locale.ROOT),
            tags = entry.frontmatter.tags,
            tokenEstimate = TokenEstimator.estimate(entry.body),
            relativePath = entry.relativePath,
            termFrequencies = termMapBuilder.build(tokens),
            length = tokens.size,
        )

    private fun buildCorpusStats(
        manifestEntries: List<ManifestEntry>,
        tokenLists: Collection<List<String>>,
    ): CorpusStats = CorpusStats(
        documentCount = manifestEntries.size,
        averageDocumentLength = averageLength(manifestEntries),
        documentFrequencies = buildDocumentFrequencies(tokenLists),
    )

    private fun averageLength(manifestEntries: List<ManifestEntry>): Double = when {
        manifestEntries.isEmpty() -> 0.0
        else -> manifestEntries.sumOf { it.length }.toDouble() / manifestEntries.size
    }

    private fun buildDocumentFrequencies(tokenLists: Collection<List<String>>): Map<String, Int> {
        val frequencies = HashMap<String, Int>()
        for (tokens in tokenLists) {
            for (term in tokens.toSet()) {
                frequencies[term] = (frequencies[term] ?: 0) + 1
            }
        }
        return frequencies.entries
            .sortedBy { it.key }
            .associateTo(LinkedHashMap()) { it.key to it.value }
    }
}
