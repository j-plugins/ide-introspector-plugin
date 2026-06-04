package com.github.xepozz.ide.introspector.context

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CorpusLoaderDeriveTest {

    private fun load(vararg files: Pair<String, String>): LoadResult =
        CorpusLoader(FileSource { files.map { RawFile(it.first, it.second) } }).load()

    @Test
    fun derivesMetadataForGeneratedFileWithoutFrontmatter() {
        val result = load("generated/sdk-platform/services.md" to "# Services\n\nbody about services")

        val entry = result.entries.single()
        assertEquals("sdk.services", entry.id)
        assertEquals("sdk.services", entry.frontmatter.id)
        assertEquals("Services", entry.frontmatter.title)
        assertEquals(Source.GENERATED, entry.source)
        assertEquals(Source.GENERATED, entry.frontmatter.source)
        assertEquals(Kind.REFERENCE, entry.frontmatter.kind)
        assertTrue("sdk-platform" in entry.frontmatter.tags)
        assertTrue(result.issues.none { it.severity == Severity.ERROR })
    }

    @Test
    fun derivesNestedIdFromPath() {
        val result = load("generated/sdk-platform/services/types.md" to "# Types\n\nbody")
        assertEquals("sdk.services.types", result.entries.single().id)
    }

    @Test
    fun titleFallsBackToHumanizedSlugWhenNoHeading() {
        val result = load("generated/sdk-platform/virtual-files.md" to "body without a heading")
        assertEquals("Virtual Files", result.entries.single().frontmatter.title)
    }

    @Test
    fun manualFileWithoutFrontmatterIsError() {
        val result = load("manual/foo.md" to "just body, no frontmatter")

        assertTrue(result.entries.isEmpty())
        assertTrue(result.issues.any { it.code == IssueCode.MISSING_FRONTMATTER && it.severity == Severity.ERROR })
    }

    @Test
    fun manualFileWithFrontmatterIsUsedAsAuthored() {
        val markdown = "---\nid: my-skill\ntitle: My Skill\nsource: manual\nkind: skill\ndescription: does things\n---\nbody"
        val result = load("manual/my-skill.md" to markdown)

        val entry = result.entries.single()
        assertEquals("my-skill", entry.id)
        assertEquals(Source.MANUAL, entry.source)
        assertEquals(Kind.SKILL, entry.frontmatter.kind)
        assertNull(result.issues.firstOrNull { it.severity == Severity.ERROR })
    }
}
