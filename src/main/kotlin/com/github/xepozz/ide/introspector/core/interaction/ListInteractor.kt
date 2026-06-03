package com.github.xepozz.ide.introspector.core.interaction

import com.github.xepozz.ide.introspector.model.WidgetItem
import java.awt.Component
import java.awt.Rectangle
import javax.swing.JList

object ListInteractor : IndexedWidgetInteractor() {
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

    override fun applySelection(component: Component, index: Int) {
        val list = component as JList<*>
        list.selectedIndex = index
        list.ensureIndexIsVisible(index)
    }

    override fun boundsForIndex(component: Component, index: Int): Rectangle? {
        val list = component as JList<*>
        return list.getCellBounds(index, index)
    }
}
