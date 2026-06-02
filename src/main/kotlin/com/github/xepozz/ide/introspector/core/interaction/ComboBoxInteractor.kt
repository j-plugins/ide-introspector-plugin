package com.github.xepozz.ide.introspector.core.interaction

import com.github.xepozz.ide.introspector.model.WidgetItem
import java.awt.Component
import java.awt.Rectangle
import javax.swing.JComboBox

object ComboBoxInteractor : WidgetInteractor {
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

    override fun select(component: Component, selector: ItemSelector): InteractionOutcome {
        val comboBox = component as JComboBox<*>
        val index = resolveIndex(comboBox, selector)
            ?: return InteractionOutcome.notFound("No combo box item matched selector $selector")
        comboBox.selectedIndex = index
        val itemsAfter = listItems(comboBox)
        return InteractionOutcome(
            matchedItem = itemsAfter[index],
            selectionAfter = itemsAfter.filter { it.selected },
        )
    }

    override fun itemBounds(component: Component, selector: ItemSelector): Rectangle? = null

    private fun resolveIndex(comboBox: JComboBox<*>, selector: ItemSelector): Int? =
        ItemSelectorResolver.resolveIndex(listItems(comboBox), selector)
}
