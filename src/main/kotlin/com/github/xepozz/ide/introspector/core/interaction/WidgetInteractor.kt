package com.github.xepozz.ide.introspector.core.interaction

import com.github.xepozz.ide.introspector.model.WidgetItem
import java.awt.Component
import java.awt.Rectangle

interface WidgetInteractor {
    val widgetType: String

    fun supports(component: Component): Boolean

    fun listItems(component: Component): List<WidgetItem>

    fun select(component: Component, selector: ItemSelector): InteractionOutcome

    fun itemBounds(component: Component, selector: ItemSelector): Rectangle?
}

data class InteractionOutcome(
    val matchedItem: WidgetItem?,
    val selectionAfter: List<WidgetItem> = emptyList(),
    val warnings: List<String> = emptyList(),
) {
    val success: Boolean get() = matchedItem != null

    companion object {
        fun notFound(warning: String): InteractionOutcome =
            InteractionOutcome(matchedItem = null, warnings = listOf(warning))
    }
}
