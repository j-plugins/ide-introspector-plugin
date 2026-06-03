package com.github.xepozz.ide.introspector.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TruncationTest {

    @Test
    fun `returns the text unchanged when shorter than the limit`() {
        assertEquals("hello", truncateChars("hello", 10))
    }

    @Test
    fun `returns the text unchanged when exactly at the limit`() {
        assertEquals("hello", truncateChars("hello", 5))
    }

    @Test
    fun `truncates and appends an ellipsis when longer than the limit`() {
        assertEquals("hel…", truncateChars("hello", 3))
    }

    @Test
    fun `treats a zero limit as no truncation`() {
        assertEquals("hello", truncateChars("hello", 0))
    }

    @Test
    fun `treats a negative limit as no truncation`() {
        assertEquals("hello", truncateChars("hello", -1))
    }
}
