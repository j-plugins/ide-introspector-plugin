package com.github.xepozz.ide.introspector.model

import kotlinx.serialization.Serializable

@Serializable
data class WidgetItem(
    val index: Int,
    val text: String? = null,
    val selected: Boolean = false,
    val enabled: Boolean = true,
    val path: List<String> = emptyList(),
    val depth: Int = 0,
    val expanded: Boolean = false,
    val leaf: Boolean = true,
)

@Serializable
data class ListItemsResponse(
    val componentId: String,
    val widgetType: String,
    val items: List<WidgetItem>,
    val warnings: List<String> = emptyList(),
)

@Serializable
data class InteractionResponse(
    val componentId: String,
    val action: String,
    val success: Boolean,
    val widgetType: String? = null,
    val matchedItem: WidgetItem? = null,
    val selectionAfter: List<WidgetItem> = emptyList(),
    val warnings: List<String> = emptyList(),
)
