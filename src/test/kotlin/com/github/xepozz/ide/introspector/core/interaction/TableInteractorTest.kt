package com.github.xepozz.ide.introspector.core.interaction

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeFalse
import org.junit.Test
import java.awt.GraphicsEnvironment
import javax.swing.JButton
import javax.swing.JTable
import javax.swing.table.DefaultTableModel

class TableInteractorTest {

    private fun peopleTable(): JTable {
        val model = DefaultTableModel(arrayOf("Name", "Role"), 0)
        model.addRow(arrayOf<Any?>("Ada", "Engineer"))
        model.addRow(arrayOf<Any?>("Linus", "Maintainer"))
        model.addRow(arrayOf<Any?>("Grace", "Admiral"))
        return JTable(model)
    }

    @Test
    fun `supports recognises a JTable`() {
        assertTrue(TableInteractor.supports(peopleTable()))
    }

    @Test
    fun `supports rejects a non-table component`() {
        assertFalse(TableInteractor.supports(JButton("nope")))
    }

    @Test
    fun `listItems joins each row columns with a pipe separator`() {
        val items = TableInteractor.listItems(peopleTable())

        assertEquals(3, items.size)
        assertEquals("Ada | Engineer", items[0].text)
        assertEquals("Linus | Maintainer", items[1].text)
        assertEquals("Grace | Admiral", items[2].text)
        assertEquals(listOf(0, 1, 2), items.map { it.index })
    }

    @Test
    fun `listItems renders a null cell as an empty segment`() {
        val model = DefaultTableModel(arrayOf("Name", "Role"), 0)
        model.addRow(arrayOf<Any?>("Ada", null))
        val table = JTable(model)

        assertEquals("Ada | ", TableInteractor.listItems(table)[0].text)
    }

    @Test
    fun `select by index selects the whole row`() {
        val table = peopleTable()

        val outcome = TableInteractor.select(table, ItemSelector(index = 1))

        assertTrue(outcome.success)
        assertTrue(table.isRowSelected(1))
        assertEquals("Linus | Maintainer", outcome.matchedItem?.text)
        assertTrue(outcome.matchedItem?.selected == true)
    }

    @Test
    fun `select by text matching a cell value resolves the row`() {
        val table = peopleTable()

        val outcome = TableInteractor.select(table, ItemSelector(text = "Admiral", matchMode = "contains"))

        assertTrue(outcome.success)
        assertTrue(table.isRowSelected(2))
        assertEquals("Grace | Admiral", outcome.matchedItem?.text)
    }

    @Test
    fun `select reports selectionAfter as the selected rows`() {
        val table = peopleTable()

        val outcome = TableInteractor.select(table, ItemSelector(index = 0))

        assertEquals(listOf(0), outcome.selectionAfter.map { it.index })
        assertTrue(outcome.selectionAfter.all { it.selected })
    }

    @Test
    fun `select with no match returns notFound and leaves selection untouched`() {
        val table = peopleTable()
        table.setRowSelectionInterval(2, 2)

        val outcome = TableInteractor.select(table, ItemSelector(text = "Nobody", matchMode = "exact"))

        assertFalse(outcome.success)
        assertNull(outcome.matchedItem)
        assertTrue(outcome.warnings.isNotEmpty())
        assertTrue(table.isRowSelected(2))
    }

    @Test
    fun `itemBounds returns null when the selector matches nothing`() {
        val table = peopleTable()

        assertNull(TableInteractor.itemBounds(table, ItemSelector(text = "Nobody", matchMode = "exact")))
    }

    @Test
    fun `itemBounds returns a rectangle for a resolved row`() {
        assumeFalse("Requires display server for cell rect.", GraphicsEnvironment.isHeadless())
        val table = peopleTable()

        assertNotNull(TableInteractor.itemBounds(table, ItemSelector(index = 0)))
    }
}
