package com.github.xepozz.ide.introspector.core.interaction

import com.github.xepozz.ide.introspector.model.WidgetItem
import java.awt.Component
import java.awt.Rectangle
import javax.swing.JTable

object TableInteractor : WidgetInteractor {
    override val widgetType: String = "table"

    override fun supports(component: Component): Boolean = component is JTable

    override fun listItems(component: Component): List<WidgetItem> {
        val table = component as JTable
        return (0 until table.rowCount).map { rowIndex ->
            WidgetItem(
                index = rowIndex,
                text = rowText(table, rowIndex),
                selected = table.isRowSelected(rowIndex),
                enabled = table.isEnabled,
            )
        }
    }

    override fun select(component: Component, selector: ItemSelector): InteractionOutcome {
        val table = component as JTable
        val rowIndex = resolveRow(table, selector)
            ?: return InteractionOutcome.notFound("No table row matched selector $selector")

        table.setRowSelectionInterval(rowIndex, rowIndex)
        if (table.columnCount > 0) {
            table.scrollRectToVisible(table.getCellRect(rowIndex, 0, true))
        }

        val itemsAfter = listItems(table)
        return InteractionOutcome(
            matchedItem = itemsAfter.getOrNull(rowIndex),
            selectionAfter = itemsAfter.filter { it.selected },
        )
    }

    override fun itemBounds(component: Component, selector: ItemSelector): Rectangle? {
        val table = component as JTable
        val rowIndex = resolveRow(table, selector) ?: return null
        return table.getCellRect(rowIndex, 0, true)
    }

    private fun resolveRow(table: JTable, selector: ItemSelector): Int? =
        ItemSelectorResolver.resolveIndex(listItems(table), selector)

    private fun rowText(table: JTable, rowIndex: Int): String =
        (0 until table.columnCount).joinToString(" | ") { columnIndex ->
            table.getValueAt(rowIndex, columnIndex)?.toString() ?: ""
        }
}
