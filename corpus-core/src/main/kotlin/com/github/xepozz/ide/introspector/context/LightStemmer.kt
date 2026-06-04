package com.github.xepozz.ide.introspector.context

object LightStemmer : Stemmer {
    private const val MINIMUM_STEM_LENGTH = 3

    override fun stem(token: String): String {
        val candidate = applyRules(token)
        return when {
            candidate.length < MINIMUM_STEM_LENGTH -> token
            else -> candidate
        }
    }

    private fun applyRules(token: String): String = when {
        token.endsWith("ies") -> token.dropLast(3) + "y"
        token.endsWith("sses") -> token.dropLast(2)
        token.length >= 5 && token.endsWith("ing") -> token.dropLast(3)
        token.length >= 4 && token.endsWith("ed") -> token.dropLast(2)
        token.length >= 4 && token.endsWith("s") && !token.endsWith("ss") -> token.dropLast(1)
        else -> token
    }
}
