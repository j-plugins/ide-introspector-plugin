package com.github.xepozz.ide.introspector.util

fun String?.containsQuery(query: String?): Boolean =
    query == null || (this != null && contains(query, ignoreCase = true))

fun areaMatches(filter: String, value: String): Boolean =
    filter == "all" || filter == "both" || filter == value
