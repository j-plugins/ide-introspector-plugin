package com.github.xepozz.ide.introspector.context

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SdkDocsConverterSplitTest {

    private val converter = SdkDocsConverter(build = "261.0.0", maxChunkChars = 2_000)
    private val filler = "Lorem ipsum dolor sit amet consectetur. ".repeat(80)

    private fun docByPath(docs: List<GeneratedDoc>, path: String): GeneratedDoc? =
        docs.firstOrNull { it.relativePath == path }

    @Test
    fun mainIndexHoldsFullHeadingTreeWithFileLinks() {
        val text = """
            # Big Topic

            Intro paragraph.

            ## First Section

            $filler

            ### Nested

            $filler

            ## Second Section

            $filler
        """.trimIndent()

        val index = docByPath(converter.convert(text, setOf("Big Topic")), "big-topic.md")!!

        assertTrue(index.content.contains("Intro paragraph."))
        assertTrue(index.content.contains("## First Section (big-topic/first-section.md)"))
        assertTrue(index.content.contains("### Nested (big-topic/first-section/nested.md)"))
        assertTrue(index.content.contains("## Second Section (big-topic/second-section.md)"))
    }

    @Test
    fun footerIsOnlyInMainIndexNotSectionFiles() {
        val text = """
            # Big Topic

            ## First Section

            $filler

            ## Second Section

            $filler
        """.trimIndent()

        val docs = converter.convert(text, setOf("Big Topic"))

        assertTrue(docByPath(docs, "big-topic.md")!!.content.contains("> Source: IntelliJ Platform SDK docs"))
        assertFalse(docByPath(docs, "big-topic/first-section.md")!!.content.contains("> Source:"))
        assertFalse(docByPath(docs, "big-topic/second-section.md")!!.content.contains("> Source:"))
    }

    @Test
    fun sectionFileHoldsOwnBodyAndLinksToChildren() {
        val text = """
            # Big Topic

            ## Parent

            parent body text

            ### Child A

            $filler

            ### Child B

            $filler
        """.trimIndent()

        val parent = docByPath(converter.convert(text, setOf("Big Topic")), "big-topic/parent.md")!!

        assertTrue(parent.content.contains("id: sdk.big-topic.parent"))
        assertTrue(parent.content.contains("parent body text"))
        assertTrue(parent.content.contains("### Child A (big-topic/parent/child-a.md)"))
        assertTrue(parent.content.contains("### Child B (big-topic/parent/child-b.md)"))
        assertNotNull(docByPath(converter.convert(text, setOf("Big Topic")), "big-topic/parent/child-a.md"))
    }

    @Test
    fun headingLevelsMayJumpAndNestUnderNearestParent() {
        val text = """
            # Big Topic

            ## Section

            $filler

            #### Deep

            $filler
        """.trimIndent()

        val docs = converter.convert(text, setOf("Big Topic"))

        assertNotNull(docByPath(docs, "big-topic/section.md"))
        assertNotNull(docByPath(docs, "big-topic/section/deep.md"))
        assertTrue(docByPath(docs, "big-topic/section.md")!!.content.contains("#### Deep (big-topic/section/deep.md)"))
    }

    @Test
    fun emptyHeadingsDeriveTitleFromFirstMeaningfulLine() {
        val text = """
            # Big Topic

            ##

            `idea-plugin`

            $filler

            ##

            `extensions`

            $filler
        """.trimIndent()

        val docs = converter.convert(text, setOf("Big Topic"))

        assertNotNull(docByPath(docs, "big-topic/idea-plugin.md"))
        assertNotNull(docByPath(docs, "big-topic/extensions.md"))
        assertTrue(docByPath(docs, "big-topic.md")!!.content.contains("idea-plugin (big-topic/idea-plugin.md)"))
    }

    @Test
    fun smallTopicStaysSingleFileWithFooter() {
        val text = """
            # Tiny

            ## A

            one

            ## B

            two
        """.trimIndent()

        val docs = converter.convert(text, setOf("Tiny"))

        assertEquals(1, docs.size)
        assertEquals("tiny.md", docs.single().relativePath)
        assertTrue(docs.single().content.contains("> Source:"))
        assertNull(docByPath(docs, "tiny/a.md"))
    }
}
