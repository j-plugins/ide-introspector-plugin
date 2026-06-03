package com.github.xepozz.ide.introspector.util

fun truncateChars(text: String, limit: Int): String {
    if (limit <= 0 || text.length <= limit) return text
    return text.substring(0, limit) + "…"
}
