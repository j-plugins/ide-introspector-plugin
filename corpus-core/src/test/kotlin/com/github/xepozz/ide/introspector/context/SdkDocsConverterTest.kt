package com.github.xepozz.ide.introspector.context

import org.junit.Assert.*
import org.junit.Test

class SdkDocsConverterTest {
    private val build = "261.24374.151"
    private val converter = SdkDocsConverter(build)

    private fun docBySlug(docs: List<GeneratedDoc>, slug: String): GeneratedDoc =
        docs.first { it.relativePath == "$slug.md" }

    @Test
    fun only_selected_titles_are_converted() {
        val text = """
            # Threading Model
            keep me

            # Dropped Topic
            drop me

            # Kotlin Coroutines
            keep me too
        """.trimIndent()

        val docs = converter.convert(text, setOf("Threading Model", "Kotlin Coroutines"))

        val paths = docs.map { it.relativePath }.toSet()
        assertEquals(setOf("threading-model.md", "kotlin-coroutines.md"), paths)
    }

    @Test
    fun relative_path_is_slug_dot_md() {
        val docs = converter.convert("# Threading Model\nbody", setOf("Threading Model"))

        assertEquals(1, docs.size)
        assertEquals("threading-model.md", docs[0].relativePath)
    }

    @Test
    fun content_contains_expected_frontmatter_fields() {
        val docs = converter.convert("# Threading Model\nbody", setOf("Threading Model"))
        val content = docBySlug(docs, "threading-model").content

        assertTrue(content.startsWith("---\n"))
        assertTrue(content.contains("id: sdk.threading-model"))
        assertTrue(content.contains("title: Threading Model"))
        assertTrue(content.contains("source: generated"))
        assertTrue(content.contains("kind: reference"))
        assertTrue(content.contains("verifiedAgainstBuild: $build"))
    }

    @Test
    fun tags_line_includes_sdk_platform() {
        val docs = converter.convert("# Threading Model\nbody", setOf("Threading Model"))
        val content = docBySlug(docs, "threading-model").content

        val tagsLine = content.lineSequence().first { it.startsWith("tags: ") }
        assertTrue(tagsLine.startsWith("tags: ["))
        assertTrue(tagsLine.endsWith("]"))
        val inner = tagsLine.removePrefix("tags: [").removeSuffix("]")
        val tags = inner.split(",").map { it.trim() }
        assertTrue(tags.contains("sdk-platform"))
    }

    @Test
    fun relative_html_links_are_rewritten_to_absolute() {
        val body = "See [X](threading-model.html) for details."
        val docs = converter.convert("# Topic\n$body", setOf("Topic"))
        val content = docBySlug(docs, "topic").content

        assertTrue(
            content.contains("[X](https://plugins.jetbrains.com/docs/intellij/threading-model.html)"),
        )
    }

    @Test
    fun anchored_relative_html_links_keep_their_anchor() {
        val body = "See [Y](disposers.html#auto)."
        val docs = converter.convert("# Topic\n$body", setOf("Topic"))
        val content = docBySlug(docs, "topic").content

        assertTrue(
            content.contains("[Y](https://plugins.jetbrains.com/docs/intellij/disposers.html#auto)"),
        )
    }

    @Test
    fun absolute_links_are_left_unchanged() {
        val body = "Go to [Site](https://example.com/page.html)."
        val docs = converter.convert("# Topic\n$body", setOf("Topic"))
        val content = docBySlug(docs, "topic").content

        assertTrue(content.contains("[Site](https://example.com/page.html)"))
        assertFalse(content.contains("intellij/https://"))
    }

    @Test
    fun pure_anchor_links_are_left_unchanged() {
        val body = "Jump to [Z](#types)."
        val docs = converter.convert("# Topic\n$body", setOf("Topic"))
        val content = docBySlug(docs, "topic").content

        assertTrue(content.contains("[Z](#types)"))
        assertFalse(content.contains("intellij/#types"))
    }

    @Test
    fun content_ends_with_provenance_footer() {
        val docs = converter.convert("# Threading Model\nbody", setOf("Threading Model"))
        val content = docBySlug(docs, "threading-model").content

        val footer = content.trimEnd().lines().last()
        assertTrue(footer.contains("Source: IntelliJ Platform SDK docs"))
        assertTrue(footer.contains("Threading Model"))
        assertTrue(footer.contains(build))
        assertTrue(footer.contains("/llms.txt"))
    }

    @Test
    fun duplicate_titles_produce_unique_slugs() {
        val text = """
            # Dup
            first body

            # Dup
            second body
        """.trimIndent()

        val docs = converter.convert(text, setOf("Dup"))

        assertEquals(2, docs.size)
        val paths = docs.map { it.relativePath }
        assertEquals(paths.toSet().size, paths.size)
        assertTrue(paths.contains("dup.md"))
        assertTrue(paths.contains("dup-2.md"))
    }

    @Test
    fun generated_content_parses_into_valid_frontmatter() {
        val docs = converter.convert("# Threading Model\nbody", setOf("Threading Model"))
        val content = docBySlug(docs, "threading-model").content

        val parsed = FrontmatterParser.parse(content, "x.md")
        val frontmatter = parsed.frontmatter
        assertNotNull(frontmatter)
        assertEquals("sdk.threading-model", frontmatter!!.id)
        assertEquals(Source.GENERATED, frontmatter.source)
        assertEquals(Kind.REFERENCE, frontmatter.kind)

        val parseErrors = parsed.parseIssues.filter { it.severity == Severity.ERROR }
        assertTrue("unexpected parse errors: $parseErrors", parseErrors.isEmpty())

        val issues = FrontmatterValidator.validate(frontmatter, "x.md")
        val validationErrors = issues.filter { it.severity == Severity.ERROR }
        assertTrue("unexpected validation errors: $validationErrors", validationErrors.isEmpty())
    }
}
