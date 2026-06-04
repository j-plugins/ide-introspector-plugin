package com.github.xepozz.ide.introspector.context

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ManifestBuilderTest {
    private val builder = ManifestBuilder()

    private fun frontmatter(
        id: String,
        title: String? = null,
        source: Source? = null,
        kind: Kind? = null,
        tags: List<String> = emptyList(),
    ): Frontmatter = Frontmatter(
        id = id,
        title = title,
        source = source,
        kind = kind,
        tags = tags,
    )

    private fun entry(
        id: String,
        body: String,
        title: String? = null,
        kind: Kind? = null,
        source: Source = Source.MANUAL,
        tags: List<String> = emptyList(),
        relativePath: String = "$id.md",
        layer: CorpusLayer = CorpusLayer.MANUAL,
    ): CorpusEntry = CorpusEntry(
        id = id,
        frontmatter = frontmatter(id = id, title = title, source = source, kind = kind, tags = tags),
        body = body,
        relativePath = relativePath,
        layer = layer,
        source = source,
    )

    @Test
    fun mapsCorpusEntryToManifestEntry() {
        val entry = entry(
            id = "alpha",
            body = "LocalInspectionTool registers tools",
            title = "Alpha Title",
            kind = Kind.SKILL,
            source = Source.GENERATED,
            tags = listOf("t1", "t2"),
            relativePath = "docs/alpha.md",
        )
        val manifest = builder.build(listOf(entry), generatedForBuild = "IU-252")
        val manifestEntry = manifest.entries.single()

        assertEquals("alpha", manifestEntry.id)
        assertEquals("Alpha Title", manifestEntry.title)
        assertEquals("skill", manifestEntry.kind)
        assertEquals("generated", manifestEntry.source)
        assertEquals(listOf("t1", "t2"), manifestEntry.tags)
        assertEquals("docs/alpha.md", manifestEntry.relativePath)
        assertEquals(Tokenizer().tokenize(entry.body).size, manifestEntry.length)
    }

    @Test
    fun titleFallsBackToIdWhenFrontmatterTitleNull() {
        val manifest = builder.build(listOf(entry(id = "beta", body = "tools")), "build")
        assertEquals("beta", manifest.entries.single().title)
    }

    @Test
    fun kindFallsBackToReferenceWhenNull() {
        val manifest = builder.build(listOf(entry(id = "gamma", body = "tools", kind = null)), "build")
        assertEquals("reference", manifest.entries.single().kind)
    }

    @Test
    fun entriesAreSortedById() {
        val entries = listOf(
            entry(id = "charlie", body = "tools"),
            entry(id = "alpha", body = "tools"),
            entry(id = "bravo", body = "tools"),
        )
        val manifest = builder.build(entries, "build")
        assertEquals(listOf("alpha", "bravo", "charlie"), manifest.entries.map { it.id })
    }

    @Test
    fun headerReflectsEntryCount() {
        val entries = listOf(entry("a", "tools"), entry("b", "tools"))
        val manifest = builder.build(entries, "IU-9")
        assertEquals(2, manifest.header.entryCount)
        assertEquals("IU-9", manifest.header.generatedForBuild)
    }

    @Test
    fun documentCountEqualsEntrySize() {
        val entries = listOf(entry("a", "tools"), entry("b", "tools"), entry("c", "tools"))
        val manifest = builder.build(entries, "build")
        assertEquals(3, manifest.corpusStats.documentCount)
    }

    @Test
    fun averageDocumentLengthIsZeroForEmptyInput() {
        val manifest = builder.build(emptyList(), "build")
        assertEquals(0.0, manifest.corpusStats.averageDocumentLength, 0.0)
        assertEquals(0, manifest.corpusStats.documentCount)
        assertTrue(manifest.corpusStats.documentFrequencies.isEmpty())
    }

    @Test
    fun documentFrequenciesCountEachTermOncePerDocument() {
        val entries = listOf(
            entry("a", "inspection inspection inspection"),
            entry("b", "inspection tools"),
        )
        val manifest = builder.build(entries, "build")
        val frequencies = manifest.corpusStats.documentFrequencies
        assertEquals(2, frequencies["inspection"])
        assertEquals(1, frequencies["tool"])
    }

    @Test
    fun documentFrequenciesKeysAreAscendingSorted() {
        val entries = listOf(
            entry("a", "zebra mango apple"),
            entry("b", "apple delta"),
        )
        val manifest = builder.build(entries, "build")
        val keys = manifest.corpusStats.documentFrequencies.keys.toList()
        assertEquals(keys.sorted(), keys)
    }

    @Test
    fun termFrequenciesKeysAreAscendingSorted() {
        val manifest = builder.build(listOf(entry("a", "zebra mango apple bravo")), "build")
        val keys = manifest.entries.single().termFrequencies.keys.toList()
        assertEquals(keys.sorted(), keys)
    }

    @Test
    fun emptyBodyEntryStillIndexedWithZeroLength() {
        val manifest = builder.build(listOf(entry("empty", "")), "build")
        val manifestEntry = manifest.entries.single()
        assertEquals(0, manifestEntry.length)
        assertTrue(manifestEntry.termFrequencies.isEmpty())
        assertEquals(0, manifestEntry.tokenEstimate)
    }

    @Test
    fun manifestSerializationIsDeterministicAcrossShuffledInput() {
        val entries = listOf(
            entry("alpha", "LocalInspectionTool registers tools", title = "A", kind = Kind.SKILL),
            entry("bravo", "com.intellij.openapi.Foo running inspections", kind = Kind.CONCEPT),
            entry("charlie", "PsiClass parsing status", source = Source.GENERATED),
            entry("delta", "", tags = listOf("x")),
        )
        val ordered = builder.build(entries, "IU-252")
        val shuffled = builder.build(entries.reversed(), "IU-252")

        val orderedJson = Json.encodeToString(ordered)
        val shuffledJson = Json.encodeToString(shuffled)

        assertEquals(orderedJson, shuffledJson)
    }

    @Test
    fun differentEntriesProduceDifferentManifests() {
        val one = builder.build(listOf(entry("a", "tools")), "build")
        val two = builder.build(listOf(entry("b", "inspections")), "build")
        assertNotEquals(Json.encodeToString(one), Json.encodeToString(two))
    }
}
