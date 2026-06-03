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

abstract class IndexedWidgetInteractor : WidgetInteractor {
    final override fun select(component: Component, selector: ItemSelector): InteractionOutcome {
        val index = ItemSelectorResolver.resolveIndex(listItems(component), selector)
            ?: return InteractionOutcome.notFound("No $widgetType item matched selector $selector")
        applySelection(component, index)
        val itemsAfter = listItems(component)
        return InteractionOutcome(
            matchedItem = itemsAfter.getOrNull(index),
            selectionAfter = itemsAfter.filter { it.selected },
        )
    }

    final override fun itemBounds(component: Component, selector: ItemSelector): Rectangle? =
        ItemSelectorResolver.resolveIndex(listItems(component), selector)
            ?.let { boundsForIndex(component, it) }

    protected abstract fun applySelection(component: Component, index: Int)

    protected open fun boundsForIndex(component: Component, index: Int): Rectangle? = null
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
