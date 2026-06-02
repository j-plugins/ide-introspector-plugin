package com.github.xepozz.ide.introspector.core.interaction

import com.github.xepozz.ide.introspector.model.WidgetItem

data class ItemSelector(
    val index: Int? = null,
    val text: String? = null,
    val matchMode: String = "exact",
    val path: List<String>? = null,
) {
    companion object {
        fun of(index: Int, text: String?, matchMode: String, path: List<String>): ItemSelector =
            ItemSelector(
                index = index.takeIf { it >= 0 },
                text = text?.takeIf { it.isNotEmpty() },
                matchMode = matchMode,
                path = path.takeIf { it.isNotEmpty() },
            )
    }
}

object ItemTextMatcher {
    fun matches(candidate: String?, query: String, matchMode: String): Boolean {
        if (candidate == null) return false
        return when (matchMode) {
            "exact" -> candidate == query
            "regex" -> Regex(query).containsMatchIn(candidate)
            else -> candidate.contains(query, ignoreCase = true)
        }
    }
}

object ItemSelectorResolver {
    fun resolveIndex(items: List<WidgetItem>, selector: ItemSelector): Int? {
        selector.index?.let { requested ->
            return requested.takeIf { it in items.indices }
        }
        val query = selector.text ?: return null
        return items
            .firstOrNull { ItemTextMatcher.matches(it.text, query, selector.matchMode) }
            ?.index
    }
}
