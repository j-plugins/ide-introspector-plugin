package com.github.xepozz.ide.introspector.core.interaction

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeFalse
import org.junit.Test
import java.awt.GraphicsEnvironment
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JList

class ListInteractorTest {

    private fun jListOf(vararg values: String): JList<String> {
        val model = DefaultListModel<String>()
        values.forEach { model.addElement(it) }
        return JList(model)
    }

    @Test
    fun `supports recognises a JList`() {
        assertTrue(ListInteractor.supports(jListOf("only")))
    }

    @Test
    fun `supports rejects a non-list component`() {
        assertFalse(ListInteractor.supports(JButton("nope")))
    }

    @Test
    fun `listItems mirrors the model contents and order`() {
        val list = jListOf("alpha", "beta", "gamma")
        val items = ListInteractor.listItems(list)

        assertEquals(3, items.size)
        assertEquals(listOf("alpha", "beta", "gamma"), items.map { it.text })
        assertEquals(listOf(0, 1, 2), items.map { it.index })
        assertTrue(items.all { it.enabled })
    }

    @Test
    fun `listItems reports an empty model as no items`() {
        assertEquals(emptyList<String>(), ListInteractor.listItems(jListOf()).map { it.text })
    }

    @Test
    fun `listItems reflects a pre-existing selection`() {
        val list = jListOf("alpha", "beta", "gamma")
        list.selectedIndex = 1

        val items = ListInteractor.listItems(list)

        assertFalse(items[0].selected)
        assertTrue(items[1].selected)
        assertFalse(items[2].selected)
    }

    @Test
    fun `select by index moves the selection to that row`() {
        val list = jListOf("alpha", "beta", "gamma")

        val outcome = ListInteractor.select(list, ItemSelector(index = 2))

        assertTrue(outcome.success)
        assertEquals(2, list.selectedIndex)
        assertEquals("gamma", outcome.matchedItem?.text)
        assertTrue(outcome.matchedItem?.selected == true)
    }

    @Test
    fun `select by exact text resolves the matching row`() {
        val list = jListOf("alpha", "beta", "gamma")

        val outcome = ListInteractor.select(list, ItemSelector(text = "beta", matchMode = "exact"))

        assertTrue(outcome.success)
        assertEquals(1, list.selectedIndex)
        assertEquals("beta", outcome.matchedItem?.text)
    }

    @Test
    fun `select by contains text resolves the first partial match`() {
        val list = jListOf("alpha", "beta", "betamax")

        val outcome = ListInteractor.select(list, ItemSelector(text = "beta", matchMode = "contains"))

        assertTrue(outcome.success)
        assertEquals(1, list.selectedIndex)
        assertEquals("beta", outcome.matchedItem?.text)
    }

    @Test
    fun `select reports selectionAfter as the currently selected rows`() {
        val list = jListOf("alpha", "beta", "gamma")

        val outcome = ListInteractor.select(list, ItemSelector(index = 0))

        assertEquals(listOf(0), outcome.selectionAfter.map { it.index })
        assertTrue(outcome.selectionAfter.all { it.selected })
    }

    @Test
    fun `select with no match returns notFound and leaves selection untouched`() {
        val list = jListOf("alpha", "beta", "gamma")
        list.selectedIndex = 1

        val outcome = ListInteractor.select(list, ItemSelector(text = "missing", matchMode = "exact"))

        assertFalse(outcome.success)
        assertNull(outcome.matchedItem)
        assertTrue(outcome.warnings.isNotEmpty())
        assertEquals(1, list.selectedIndex)
    }

    @Test
    fun `select with an out-of-range index returns notFound`() {
        val list = jListOf("alpha")

        val outcome = ListInteractor.select(list, ItemSelector(index = 9))

        assertFalse(outcome.success)
    }

    @Test
    fun `itemBounds returns null when the selector matches nothing`() {
        val list = jListOf("alpha")

        assertNull(ListInteractor.itemBounds(list, ItemSelector(text = "missing", matchMode = "exact")))
    }

    @Test
    fun `itemBounds returns a rectangle for a realized cell`() {
        assumeFalse("Requires display server for cell bounds.", GraphicsEnvironment.isHeadless())
        val list = jListOf("alpha", "beta")

        val bounds = ListInteractor.itemBounds(list, ItemSelector(index = 0))

        assertNotNull(bounds)
    }
}
