package com.github.xepozz.ide.introspector.context

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SdkDocsConverterSplitTest {

    private val converter = SdkDocsConverter(build = "261.0.0", maxChunkChars = 2_000, minSplitChars = 500)
    private val filler = "Lorem ipsum dolor sit amet consectetur. ".repeat(80)

    private fun docByPath(docs: List<GeneratedDoc>, path: String): GeneratedDoc? =
        docs.firstOrNull { it.relativePath == path }

    @Test
    fun largeSectionsBecomeFilesLinkedByIdFromMainIndex() {
        val text = "# Big Topic\n\nIntro paragraph.\n\n## Big One\n\n$filler\n\n## Big Two\n\n$filler"

        val docs = converter.convert(text, setOf("Big Topic"))
        val index = docByPath(docs, "big-topic.md")!!

        assertTrue(index.content.startsWith("# Big Topic"))
        assertTrue(index.content.contains("Intro paragraph."))
        assertTrue(index.content.contains("## Big One (sdk.big-topic.big-one)"))
        assertTrue(index.content.contains("## Big Two (sdk.big-topic.big-two)"))
        val bigOne = docByPath(docs, "big-topic/big-one.md")!!
        assertTrue(bigOne.content.startsWith("# Big One"))
        assertFalse(bigOne.content.startsWith("---"))
    }

    @Test
    fun smallSectionsStayInlineNotSeparateFiles() {
        val text = "# Big Topic\n\n## Big One\n\n$filler\n\n## Note\n\njust a short note here\n\n## Big Two\n\n$filler"

        val docs = converter.convert(text, setOf("Big Topic"))
        val index = docByPath(docs, "big-topic.md")!!

        assertTrue(index.content.contains("## Note"))
        assertFalse(index.content.contains("## Note (sdk"))
        assertTrue(index.content.contains("just a short note here"))
        assertNull(docByPath(docs, "big-topic/note.md"))
    }

    @Test
    fun footerOnlyInMainIndexNotSectionFiles() {
        val text = "# Big Topic\n\n## Big One\n\n$filler\n\n## Big Two\n\n$filler"

        val docs = converter.convert(text, setOf("Big Topic"))

        assertTrue(docByPath(docs, "big-topic.md")!!.content.contains("> Source: IntelliJ Platform SDK docs"))
        assertFalse(docByPath(docs, "big-topic/big-one.md")!!.content.contains("> Source:"))
    }

    @Test
    fun sectionFileLinksToLargeChildrenById() {
        val text = "# Big Topic\n\n## Parent\n\nparent intro text\n\n### Child A\n\n$filler\n\n### Child B\n\n$filler"

        val docs = converter.convert(text, setOf("Big Topic"))
        val parent = docByPath(docs, "big-topic/parent.md")!!

        assertTrue(parent.content.startsWith("# Parent"))
        assertTrue(parent.content.contains("parent intro text"))
        assertTrue(parent.content.contains("### Child A (sdk.big-topic.parent.child-a)"))
        assertNotNull(docByPath(docs, "big-topic/parent/child-a.md"))
        assertNotNull(docByPath(docs, "big-topic/parent/child-b.md"))
    }

    @Test
    fun emptyHeadingsDeriveTitleFromFirstMeaningfulLine() {
        val text = "# Big Topic\n\n##\n\n`idea-plugin`\n\n$filler\n\n##\n\n`extensions`\n\n$filler"

        val docs = converter.convert(text, setOf("Big Topic"))

        assertNotNull(docByPath(docs, "big-topic/idea-plugin.md"))
        assertNotNull(docByPath(docs, "big-topic/extensions.md"))
        assertTrue(docByPath(docs, "big-topic.md")!!.content.contains("idea-plugin (sdk.big-topic.idea-plugin)"))
    }

    @Test
    fun smallTopicStaysSingleFileWithFooter() {
        val text = "# Tiny\n\n## A\n\none\n\n## B\n\ntwo"

        val docs = converter.convert(text, setOf("Tiny"))

        assertEquals(1, docs.size)
        assertEquals("tiny.md", docs.single().relativePath)
        assertTrue(docs.single().content.contains("> Source:"))
        assertNull(docByPath(docs, "tiny/a.md"))
    }
}
