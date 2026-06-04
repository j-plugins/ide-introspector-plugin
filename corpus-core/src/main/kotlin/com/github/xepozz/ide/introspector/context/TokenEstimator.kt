package com.github.xepozz.ide.introspector.context

import kotlin.math.ceil

object TokenEstimator {
    private const val CHARACTERS_PER_TOKEN = 4.0

    fun estimate(text: String): Int {
        if (text.isBlank()) return 0
        val codePointCount = text.codePointCount(0, text.length)
        return maxOf(1, ceil(codePointCount / CHARACTERS_PER_TOKEN).toInt())
    }
}
