package com.github.xepozz.ide.introspector.core.interaction

import com.github.xepozz.ide.introspector.model.WidgetItem
import java.awt.Component
import java.awt.Rectangle
import javax.swing.JTable

object TableInteractor : IndexedWidgetInteractor() {
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

    override fun applySelection(component: Component, index: Int) {
        val table = component as JTable
        table.setRowSelectionInterval(index, index)
        if (table.columnCount > 0) {
            table.scrollRectToVisible(table.getCellRect(index, 0, true))
        }
    }

    override fun boundsForIndex(component: Component, index: Int): Rectangle? {
        val table = component as JTable
        return table.getCellRect(index, 0, true)
    }

    private fun rowText(table: JTable, rowIndex: Int): String =
        (0 until table.columnCount).joinToString(" | ") { columnIndex ->
            table.getValueAt(rowIndex, columnIndex)?.toString() ?: ""
        }
}
