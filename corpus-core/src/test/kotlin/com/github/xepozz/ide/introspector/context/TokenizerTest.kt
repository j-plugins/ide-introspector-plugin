package com.github.xepozz.ide.introspector.context

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenizerTest {
    private val tokenizer = Tokenizer()

    @Test
    fun lowercasesTokens() {
        assertEquals(listOf("hello"), tokenizer.tokenize("HELLO"))
    }

    @Test
    fun splitsOnNonAlphanumeric() {
        assertEquals(listOf("foo", "bar", "baz"), tokenizer.tokenize("foo-bar_baz"))
    }

    @Test
    fun emitsWholeDottedTokenAndEachSegment() {
        val tokens = tokenizer.tokenize("com.intellij.openapi.Foo")
        assertTrue(tokens.contains("com"))
        assertTrue(tokens.contains("intellij"))
        assertTrue(tokens.contains("openapi"))
        assertTrue(tokens.contains("foo"))
        assertTrue(tokens.any { it.contains('.') })
    }

    @Test
    fun keepsDottedRunSegmentsTogether() {
        val tokens = tokenizer.tokenize("com.intellij.openapi.Foo")
        assertEquals(
            listOf("com.intellij.openapi.foo", "com", "intellij", "openapi", "foo"),
            tokens,
        )
    }

    @Test
    fun splitsCamelCaseRun() {
        val tokens = tokenizer.tokenize("LocalInspectionTool")
        assertEquals(listOf("local", "inspection", "tool"), tokens)
    }

    @Test
    fun splitsAcronymFollowedByWord() {
        assertEquals(listOf("http", "server"), tokenizer.tokenize("HTTPServer"))
    }

    @Test
    fun splitsOnDigitBoundaryAndDropsShortNumber() {
        assertEquals(listOf("psi", "class"), tokenizer.tokenize("PsiClass2"))
    }

    @Test
    fun removesStopWordsAfterSplitting() {
        assertEquals(listOf("quick", "fox"), tokenizer.tokenize("the quick and fox"))
    }

    @Test
    fun dropsTokensShorterThanMinimumLength() {
        assertEquals(emptyList<String>(), tokenizer.tokenize("a b c"))
    }

    @Test
    fun returnsEmptyListForEmptyText() {
        assertEquals(emptyList<String>(), tokenizer.tokenize(""))
    }

    @Test
    fun returnsEmptyListForBlankText() {
        assertEquals(emptyList<String>(), tokenizer.tokenize("   \t\n  "))
    }

    @Test
    fun producesDeterministicOutputAcrossCalls() {
        val text = "LocalInspectionTool registers com.intellij.openapi.Foo and HTTPServer"
        assertEquals(tokenizer.tokenize(text), tokenizer.tokenize(text))
    }

    @Test
    fun honorsCustomMinimumLength() {
        val custom = Tokenizer(stopWords = emptySet(), stemmer = LightStemmer, minimumLength = 4)
        assertEquals(listOf("quick"), custom.tokenize("the quick fox"))
    }

    @Test
    fun honorsCustomStopWords() {
        val custom = Tokenizer(stopWords = setOf("quick"))
        assertEquals(listOf("fox"), custom.tokenize("quick fox"))
    }
}
