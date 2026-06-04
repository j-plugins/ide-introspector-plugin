package com.github.xepozz.ide.introspector.context

import org.junit.Assert.*
import org.junit.Test

class LlmsTopicSplitterTest {
    @Test
    fun splits_each_h1_into_its_own_topic() {
        val text = """
            # First
            body of first

            # Second
            body of second
        """.trimIndent()

        val topics = LlmsTopicSplitter.split(text)

        assertEquals(2, topics.size)
        assertEquals("First", topics[0].title)
        assertEquals("body of first", topics[0].body)
        assertEquals("Second", topics[1].title)
        assertEquals("body of second", topics[1].body)
    }

    @Test
    fun title_is_text_after_hash_space() {
        val topics = LlmsTopicSplitter.split("# Threading Model\ncontent")

        assertEquals(1, topics.size)
        assertEquals("Threading Model", topics[0].title)
        assertEquals("content", topics[0].body)
    }

    @Test
    fun content_before_first_h1_is_ignored() {
        val text = """
            preamble that should be dropped
            more preamble

            # Real Topic
            real body
        """.trimIndent()

        val topics = LlmsTopicSplitter.split(text)

        assertEquals(1, topics.size)
        assertEquals("Real Topic", topics[0].title)
        assertEquals("real body", topics[0].body)
    }

    @Test
    fun subheadings_stay_inside_topic_body() {
        val text = """
            # Parent
            intro

            ## Subsection
            sub body

            ### Deeper
            deeper body
        """.trimIndent()

        val topics = LlmsTopicSplitter.split(text)

        assertEquals(1, topics.size)
        assertEquals("Parent", topics[0].title)
        assertTrue(topics[0].body.contains("## Subsection"))
        assertTrue(topics[0].body.contains("### Deeper"))
        assertTrue(topics[0].body.contains("sub body"))
    }

    @Test
    fun trailing_topic_without_trailing_newline_is_captured() {
        val text = "# Only\nlast line without newline"

        val topics = LlmsTopicSplitter.split(text)

        assertEquals(1, topics.size)
        assertEquals("Only", topics[0].title)
        assertEquals("last line without newline", topics[0].body)
    }

    @Test
    fun body_is_trimmed_of_surrounding_blank_lines() {
        val text = "# Trimmed\n\n\n   body   \n\n\n# Next\nx"

        val topics = LlmsTopicSplitter.split(text)

        assertEquals("body", topics[0].body)
    }

    @Test
    fun empty_input_produces_empty_list() {
        assertTrue(LlmsTopicSplitter.split("").isEmpty())
    }

    @Test
    fun input_without_any_h1_produces_empty_list() {
        assertTrue(LlmsTopicSplitter.split("just text\n## not an h1").isEmpty())
    }
}
