package com.github.xepozz.ide.introspector.context

import kotlin.math.ceil

class TokenBudgeter(private val charactersPerToken: Int = 4) {
    fun clamp(body: String, maxTokens: Int, offset: Int = 0): BudgetedSection {
        val start = offset.coerceIn(0, body.length)
        if (start >= body.length) {
            return BudgetedSection(text = "", returnedTokens = 0, truncated = false, nextOffset = null)
        }
        if (maxTokens <= 0) {
            return BudgetedSection(text = "", returnedTokens = 0, truncated = true, nextOffset = start)
        }
        val remaining = body.substring(start)
        val maxCharacters = (maxTokens * charactersPerToken).coerceAtLeast(1)
        val candidateEnd = minOf(maxCharacters, remaining.length)
        val chosenEnd = resolveEnd(remaining, candidateEnd)
        val text = remaining.substring(0, chosenEnd)
        val nextOffset = if (start + chosenEnd < body.length) start + chosenEnd else null
        return BudgetedSection(
            text = text,
            returnedTokens = tokensFor(chosenEnd),
            truncated = nextOffset != null,
            nextOffset = nextOffset,
        )
    }

    private fun resolveEnd(remaining: String, candidateEnd: Int): Int {
        if (candidateEnd >= remaining.length) {
            return candidateEnd
        }
        val paragraphBoundary = remaining.lastIndexOf(PARAGRAPH_SEPARATOR, candidateEnd - 1)
        if (paragraphBoundary < 0) {
            return candidateEnd
        }
        val boundaryEnd = paragraphBoundary + PARAGRAPH_SEPARATOR.length
        return if (boundaryEnd in 1..candidateEnd) boundaryEnd else candidateEnd
    }

    private fun tokensFor(characters: Int): Int =
        if (characters == 0) 0 else ceil(characters / charactersPerToken.toDouble()).toInt()

    private companion object {
        const val PARAGRAPH_SEPARATOR = "\n\n"
    }
}
