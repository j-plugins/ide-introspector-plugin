package com.github.xepozz.ide.introspector.context

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.ceil

class TokenBudgeterTest {
    private val charactersPerToken = 4
    private val budgeter = TokenBudgeter(charactersPerToken)

    private fun expectedTokens(characters: Int): Int =
        if (characters == 0) 0 else ceil(characters / charactersPerToken.toDouble()).toInt()

    @Test
    fun offsetPastEndReturnsEmptySection() {
        val body = "short body"
        val section = budgeter.clamp(body, maxTokens = 10, offset = body.length)
        assertEquals("", section.text)
        assertEquals(0, section.returnedTokens)
        assertFalse(section.truncated)
        assertNull(section.nextOffset)
    }

    @Test
    fun offsetBeyondLengthIsClampedToEnd() {
        val body = "abc"
        val section = budgeter.clamp(body, maxTokens = 10, offset = 999)
        assertEquals("", section.text)
        assertEquals(0, section.returnedTokens)
        assertFalse(section.truncated)
        assertNull(section.nextOffset)
    }

    @Test
    fun bodyShorterThanBudgetReturnsWholeBody() {
        val body = "tiny"
        val section = budgeter.clamp(body, maxTokens = 100)
        assertEquals(body, section.text)
        assertFalse(section.truncated)
        assertNull(section.nextOffset)
    }

    @Test
    fun bodyLongerThanBudgetCutsAtParagraphBoundary() {
        val firstParagraph = "first paragraph"
        val body = firstParagraph + "\n\n" + "x".repeat(200)
        val boundaryEnd = firstParagraph.length + 2
        val maxTokens = (firstParagraph.length + 5) / charactersPerToken + 1
        val section = budgeter.clamp(body, maxTokens = maxTokens)
        assertEquals(firstParagraph + "\n\n", section.text)
        assertTrue(section.truncated)
        assertEquals(boundaryEnd, section.nextOffset)
    }

    @Test
    fun singleParagraphLargerThanBudgetIsHardCut() {
        val body = "a".repeat(100)
        val maxTokens = 5
        val section = budgeter.clamp(body, maxTokens = maxTokens)
        assertTrue(section.text.isNotEmpty())
        assertEquals(maxTokens * charactersPerToken, section.text.length)
        assertTrue(section.truncated)
        assertEquals(maxTokens * charactersPerToken, section.nextOffset)
    }

    @Test
    fun paginationRoundTripReconstructsBodyExactly() {
        val body = buildString {
            append("Intro paragraph about inspection.\n\n")
            append("Second paragraph about service registration.\n\n")
            append("z".repeat(53))
            append("\n\nFinal short tail.")
        }
        val rebuilt = StringBuilder()
        var offset: Int? = 0
        var guard = 0
        while (offset != null) {
            guard++
            assertTrue("pagination did not terminate", guard < 10_000)
            val section = budgeter.clamp(body, maxTokens = 3, offset = offset)
            rebuilt.append(section.text)
            offset = section.nextOffset
        }
        assertEquals(body, rebuilt.toString())
    }

    @Test
    fun returnedTokensEqualsCeilOfReturnedChars() {
        val body = "a".repeat(100)
        val maxTokens = 7
        val section = budgeter.clamp(body, maxTokens = maxTokens)
        assertEquals(expectedTokens(section.text.length), section.returnedTokens)
    }

    @Test
    fun returnedTokensCeilForNonMultipleParagraphCut() {
        val firstParagraph = "abcde"
        val body = firstParagraph + "\n\n" + "y".repeat(200)
        val maxTokens = 5
        val section = budgeter.clamp(body, maxTokens = maxTokens)
        assertEquals(firstParagraph + "\n\n", section.text)
        assertEquals(expectedTokens(section.text.length), section.returnedTokens)
    }

    @Test
    fun maxTokensZeroReturnsEmptyTextAndNextOffsetAtStart() {
        val body = "non empty body"
        val offset = 3
        val section = budgeter.clamp(body, maxTokens = 0, offset = offset)
        assertEquals("", section.text)
        assertEquals(0, section.returnedTokens)
        assertTrue(section.truncated)
        assertEquals(offset, section.nextOffset)
    }

    @Test
    fun negativeMaxTokensReturnsEmptyTextAndNextOffsetAtStart() {
        val body = "non empty body"
        val section = budgeter.clamp(body, maxTokens = -5, offset = 0)
        assertEquals("", section.text)
        assertEquals(0, section.nextOffset)
    }
}
