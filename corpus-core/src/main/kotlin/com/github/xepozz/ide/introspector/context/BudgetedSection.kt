package com.github.xepozz.ide.introspector.context

data class BudgetedSection(
    val text: String,
    val returnedTokens: Int,
    val truncated: Boolean,
    val nextOffset: Int?,
)
