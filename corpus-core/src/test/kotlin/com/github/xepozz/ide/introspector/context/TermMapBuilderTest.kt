package com.github.xepozz.ide.introspector.context

import org.junit.Assert.assertEquals
import org.junit.Test

class TermMapBuilderTest {
    @Test
    fun returnsTopNTermsByFrequency()  {
        val tokens = listOf("a", "a", "a", "b", "b", "c")
        val result = TermMapBuilder(topN = 2).build(tokens)
        assertEquals(setOf("a", "b"), result.keys)
        assertEquals(3, result["a"])
        assertEquals(2, result["b"])
    }

    @Test
    fun breaksFrequencyTiesByAscendingTerm() {
        val tokens = listOf("zebra", "apple", "mango")
        val result = TermMapBuilder(topN = 2).build(tokens)
        assertEquals(setOf("apple", "mango"), result.keys)
    }

    @Test
    fun keepsAllTermsWhenFewerThanTopN() {
        val tokens = listOf("x", "y", "y")
        val result = TermMapBuilder(topN = 10).build(tokens)
        assertEquals(2, result.size)
        assertEquals(1, result["x"])
        assertEquals(2, result["y"])
    }

    @Test
    fun returnsEmptyMapForEmptyTokens() {
        assertEquals(emptyMap<String, Int>(), TermMapBuilder(topN = 5).build(emptyList()))
    }

    @Test
    fun iterationOrderIsAscendingByKey() {
        val tokens = listOf("delta", "alpha", "charlie", "bravo", "alpha", "charlie")
        val result = TermMapBuilder(topN = 40).build(tokens)
        val keys = result.keys.toList()
        assertEquals(keys.sorted(), keys)
    }
}
