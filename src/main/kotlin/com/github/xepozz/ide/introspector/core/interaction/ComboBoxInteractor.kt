package com.github.xepozz.ide.introspector.core.interaction

import com.github.xepozz.ide.introspector.model.WidgetItem
import java.awt.Component
import javax.swing.JComboBox

object ComboBoxInteractor : IndexedWidgetInteractor() {
    override val widgetType: String = "comboBox"

    override fun supports(component: Component): Boolean = component is JComboBox<*>

    override fun listItems(component: Component): List<WidgetItem> {
        val comboBox = component as JComboBox<*>
        return (0 until comboBox.itemCount).map { index ->
            WidgetItem(
                index = index,
                text = comboBox.getItemAt(index)?.toString(),
                selected = comboBox.selectedIndex == index,
            )
        }
    }

    override fun applySelection(component: Component, index: Int) {
        val comboBox = component as JComboBox<*>
        comboBox.selectedIndex = index
    }
}
