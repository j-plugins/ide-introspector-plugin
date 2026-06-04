package com.github.xepozz.ide.introspector.context

object Slugs {
    private val NON_SLUG = Regex("[^a-z0-9]+")
    private val EDGE_DASHES = Regex("(^-+)|(-+$)")

    fun slugify(value: String): String =
        value.lowercase()
            .replace(NON_SLUG, "-")
            .replace(EDGE_DASHES, "")
}
