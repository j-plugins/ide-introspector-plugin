package com.github.xepozz.ide.introspector.core.interaction

import com.github.xepozz.ide.introspector.model.WidgetItem
import java.awt.Component
import java.awt.Rectangle
import javax.swing.JList

object ListInteractor : WidgetInteractor {
    override val widgetType: String = "list"

    override fun supports(component: Component): Boolean = component is JList<*>

    override fun listItems(component: Component): List<WidgetItem> {
        val list = component as JList<*>
        val model = list.model
        return (0 until model.size).map { itemIndex ->
            WidgetItem(
                index = itemIndex,
                text = model.getElementAt(itemIndex)?.toString(),
                selected = list.isSelectedIndex(itemIndex),
                enabled = list.isEnabled,
            )
        }
    }

    override fun select(component: Component, selector: ItemSelector): InteractionOutcome {
        val list = component as JList<*>
        val rowIndex = resolveRow(list, selector)
            ?: return InteractionOutcome.notFound("No list item matched selector $selector")

        list.selectedIndex = rowIndex
        list.ensureIndexIsVisible(rowIndex)

        val itemsAfter = listItems(list)
        return InteractionOutcome(
            matchedItem = itemsAfter.getOrNull(rowIndex),
            selectionAfter = itemsAfter.filter { it.selected },
        )
    }

    override fun itemBounds(component: Component, selector: ItemSelector): Rectangle? {
        val list = component as JList<*>
        val rowIndex = resolveRow(list, selector) ?: return null
        return list.getCellBounds(rowIndex, rowIndex)
    }

    private fun resolveRow(list: JList<*>, selector: ItemSelector): Int? =
        ItemSelectorResolver.resolveIndex(listItems(list), selector)
}
