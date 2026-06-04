package com.github.xepozz.ide.introspector.context

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SdkDocsConverterTest {

    private val converter = SdkDocsConverter(build = "261.0.0")

    private fun content(text: String, vararg titles: String): String =
        converter.convert(text, titles.toSet()).single().content

    @Test
    fun smallTopicIsSingleFileWithTitleHeadingAndNoFrontmatter() {
        val docs = converter.convert("# Services\n\nShort body about services.", setOf("Services"))

        assertEquals(1, docs.size)
        val doc = docs.single()
        assertEquals("services.md", doc.relativePath)
        assertTrue(doc.content.startsWith("# Services"))
        assertFalse(doc.content.startsWith("---"))
        assertFalse(doc.content.contains("\nid: "))
        assertTrue(doc.content.contains("Short body about services."))
    }

    @Test
    fun singleFileEndsWithProvenanceFooter() {
        val text = content("# Services\n\nbody", "Services")
        assertTrue(
            text.contains(
                "> Source: IntelliJ Platform SDK docs — Services (build 261.0.0). https://plugins.jetbrains.com/docs/intellij/llms.txt",
            ),
        )
    }

    @Test
    fun onlySelectedTitlesAreConverted() {
        val docs = converter.convert("# Services\n\nbody\n\n# Other\n\nother", setOf("Services"))
        assertEquals(1, docs.size)
        assertEquals("services.md", docs.single().relativePath)
    }

    @Test
    fun relativeHtmlLinksBecomeAbsoluteOthersUnchanged() {
        val text = content(
            "# Services\n\nSee [T](threading-model.html), [D](disposers.html#auto), [E](https://x.dev), [A](#types).",
            "Services",
        )
        assertTrue(text.contains("[T](https://plugins.jetbrains.com/docs/intellij/threading-model.html)"))
        assertTrue(text.contains("[D](https://plugins.jetbrains.com/docs/intellij/disposers.html#auto)"))
        assertTrue(text.contains("[E](https://x.dev)"))
        assertTrue(text.contains("[A](#types)"))
    }
}
