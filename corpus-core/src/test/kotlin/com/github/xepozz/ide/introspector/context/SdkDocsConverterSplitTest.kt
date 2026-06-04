package com.github.xepozz.ide.introspector.context

import org.junit.Assert.assertEquals
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
    fun largeTopicBecomesIndexPlusSubtopicFiles() {
        val text = """
            # Big Topic

            Intro paragraph for the topic.

            ## First Section

            $filler

            ## Second Section

            $filler
        """.trimIndent()

        val docs = converter.convert(text, setOf("Big Topic"))

        val index = docByPath(docs, "big-topic.md")
        assertNotNull(index)
        assertTrue(index!!.content.contains("## Subtopics"))
        assertTrue(index.content.contains("sdk.big-topic.first-section"))
        assertTrue(index.content.contains("sdk.big-topic.second-section"))

        val first = docByPath(docs, "big-topic/first-section.md")
        assertNotNull(first)
        assertTrue(first!!.content.contains("id: sdk.big-topic.first-section"))
        assertTrue(first.content.contains("title: Big Topic: First Section"))
        assertTrue(first.content.contains("Part of `sdk.big-topic`."))
        assertNotNull(docByPath(docs, "big-topic/second-section.md"))
    }

    @Test
    fun oversizedSectionRecursesIntoDeeperHeadings() {
        val text = """
            # Big Topic

            ## Huge Section

            ### Alpha

            $filler

            ### Beta

            $filler

            ## Small Section

            short.
        """.trimIndent()

        val docs = converter.convert(text, setOf("Big Topic"))

        assertNotNull(docByPath(docs, "big-topic.md"))
        assertNotNull(docByPath(docs, "big-topic/huge-section.md"))
        assertNotNull(docByPath(docs, "big-topic/huge-section/alpha.md"))
        assertNotNull(docByPath(docs, "big-topic/huge-section/beta.md"))
        val alpha = docByPath(docs, "big-topic/huge-section/alpha.md")!!
        assertTrue(alpha.content.contains("id: sdk.big-topic.huge-section.alpha"))
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
        val index = docByPath(docs, "big-topic.md")!!
        assertTrue(index.content.contains("sdk.big-topic.idea-plugin"))
    }

    @Test
    fun smallTopicStaysSingleFile() {
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
        assertNull(docByPath(docs, "tiny/a.md"))
    }
}
