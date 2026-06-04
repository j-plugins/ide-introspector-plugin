package com.github.xepozz.ide.introspector.context

import java.util.Locale

class Tokenizer(
    private val stopWords: Set<String> = DEFAULT_STOP_WORDS,
    private val stemmer: Stemmer = LightStemmer,
    private val minimumLength: Int = 2,
) {
    fun tokenize(text: String): List<String> = buildList {
        for (run in extractRuns(text)) {
            addAll(processRun(run))
        }
    }

    private fun processRun(run: String): List<String> = buildList {
        val segments = run.split('.').filter { it.isNotEmpty() }
        if (segments.size > 1) {
            addAll(finalizeSegment(run))
            for (segment in segments) {
                addAll(finalizeSegment(segment))
            }
        } else {
            addAll(finalizeSegment(run))
        }
    }

    private fun finalizeSegment(segment: String): List<String> = buildList {
        for (piece in splitCamelCase(segment)) {
            val normalized = piece.lowercase(Locale.ROOT)
            if (normalized.length < minimumLength) continue
            if (normalized in stopWords) continue
            add(stemmer.stem(normalized))
        }
    }

    private fun splitCamelCase(segment: String): List<String> =
        segment.split(CAMEL_CASE_BOUNDARY).filter { it.isNotEmpty() }

    private fun extractRuns(text: String): List<String> = buildList {
        val codePoints = text.codePoints().toArray()
        val builder = StringBuilder()
        for (index in codePoints.indices) {
            val codePoint = codePoints[index]
            if (isRunCharacter(codePoints, index)) {
                builder.appendCodePoint(codePoint)
            } else if (builder.isNotEmpty()) {
                add(builder.toString())
                builder.setLength(0)
            }
        }
        if (builder.isNotEmpty()) add(builder.toString())
    }

    private fun isRunCharacter(codePoints: IntArray, index: Int): Boolean {
        val codePoint = codePoints[index]
        if (Character.isLetterOrDigit(codePoint)) return true
        if (codePoint != DOT_CODE_POINT) return false
        val previous = codePoints.elementAtOrNull(index - 1) ?: return false
        val next = codePoints.elementAtOrNull(index + 1) ?: return false
        return Character.isLetterOrDigit(previous) && Character.isLetterOrDigit(next)
    }

    private fun IntArray.elementAtOrNull(index: Int): Int? =
        if (index in indices) this[index] else null

    companion object {
        private val DOT_CODE_POINT = '.'.code

        private val CAMEL_CASE_BOUNDARY = Regex(
            "(?<=[\\p{Ll}\\p{Nd}])(?=\\p{Lu})" +
                "|(?<=\\p{Lu})(?=\\p{Lu}\\p{Ll})" +
                "|(?<=\\p{L})(?=\\p{Nd})" +
                "|(?<=\\p{Nd})(?=\\p{L})"
        )

        val DEFAULT_STOP_WORDS: Set<String> = setOf(
            "the", "a", "an", "is", "are", "was", "were", "be", "been", "being",
            "of", "to", "in", "for", "and", "or", "not", "this", "that", "these",
            "those", "with", "as", "by", "on", "at", "from", "it", "its", "if",
            "then", "else", "when", "which", "what", "who", "how", "can", "will",
            "would", "should", "may", "might", "must", "do", "does", "did", "has",
            "have", "had", "but", "so",
        )
    }
}
