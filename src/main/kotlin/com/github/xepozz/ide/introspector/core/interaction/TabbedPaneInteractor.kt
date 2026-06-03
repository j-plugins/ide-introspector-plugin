package com.github.xepozz.ide.introspector.core.interaction

import com.github.xepozz.ide.introspector.model.WidgetItem
import java.awt.Component
import java.awt.Rectangle
import javax.swing.JTabbedPane

object TabbedPaneInteractor : IndexedWidgetInteractor() {
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

    override fun applySelection(component: Component, index: Int) {
        val tabbedPane = component as JTabbedPane
        tabbedPane.selectedIndex = index
    }

    override fun boundsForIndex(component: Component, index: Int): Rectangle? {
        val tabbedPane = component as JTabbedPane
        return tabbedPane.getBoundsAt(index)
    }
}
