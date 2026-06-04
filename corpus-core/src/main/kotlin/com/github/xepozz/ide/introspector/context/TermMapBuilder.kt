package com.github.xepozz.ide.introspector.context

class TermMapBuilder(private val topN: Int = 40) {
    fun build(tokens: List<String>): Map<String, Int> {
        val frequencies = tokens.groupingBy { it }.eachCount()
        val selected = frequencies.entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .take(topN)
        return selected
            .sortedBy { it.key }
            .associateTo(LinkedHashMap()) { it.key to it.value }
    }
}
