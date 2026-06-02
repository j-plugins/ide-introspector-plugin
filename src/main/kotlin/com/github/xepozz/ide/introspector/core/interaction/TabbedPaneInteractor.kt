package com.github.xepozz.ide.introspector.core.interaction

import com.github.xepozz.ide.introspector.model.WidgetItem
import java.awt.Component
import java.awt.Rectangle
import javax.swing.JTabbedPane

object TabbedPaneInteractor : WidgetInteractor {
    override val widgetType: String = "tabbedPane"

    override fun supports(component: Component): Boolean = component is JTabbedPane

    override fun listItems(component: Component): List<WidgetItem> {
        val tabbedPane = component as JTabbedPane
        return (0 until tabbedPane.tabCount).map { index ->
            WidgetItem(
                index = index,
                text = tabbedPane.getTitleAt(index),
                selected = tabbedPane.selectedIndex == index,
                enabled = tabbedPane.isEnabledAt(index),
            )
        }
    }

    override fun select(component: Component, selector: ItemSelector): InteractionOutcome {
        val tabbedPane = component as JTabbedPane
        val index = resolveIndex(tabbedPane, selector)
            ?: return InteractionOutcome.notFound("No tab matched selector $selector")
        tabbedPane.selectedIndex = index
        val itemsAfter = listItems(tabbedPane)
        return InteractionOutcome(
            matchedItem = itemsAfter[index],
            selectionAfter = itemsAfter.filter { it.selected },
        )
    }

    override fun itemBounds(component: Component, selector: ItemSelector): Rectangle? {
        val tabbedPane = component as JTabbedPane
        val index = resolveIndex(tabbedPane, selector) ?: return null
        return tabbedPane.getBoundsAt(index)
    }

    private fun resolveIndex(tabbedPane: JTabbedPane, selector: ItemSelector): Int? =
        ItemSelectorResolver.resolveIndex(listItems(tabbedPane), selector)
}
