package com.github.xepozz.ide.introspector.core.interaction

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import com.github.xepozz.ide.introspector.model.WidgetItem
import javax.swing.JComboBox
import javax.swing.JPanel

class ComboBoxInteractorTest {

    private fun comboBox(): JComboBox<String> =
        JComboBox(arrayOf("Red", "Green", "Blue"))

    @Test
    fun `supports recognizes a JComboBox and rejects a plain panel`() {
        assertTrue(ComboBoxInteractor.supports(JComboBox<String>()))
        assertFalse(ComboBoxInteractor.supports(JPanel()))
    }

    @Test
    fun `listItems mirrors every entry and the current selection`() {
        val comboBox = comboBox()
        comboBox.selectedIndex = 2

        val items = ComboBoxInteractor.listItems(comboBox)

        assertEquals(3, items.size)
        assertEquals(listOf("Red", "Green", "Blue"), items.map { it.text })
        assertEquals(listOf(0, 1, 2), items.map { it.index })
        assertEquals(listOf(false, false, true), items.map { it.selected })
    }

    @Test
    fun `listItems on an empty combo box yields no items`() {
        assertEquals(emptyList<WidgetItem>(), ComboBoxInteractor.listItems(JComboBox<String>()))
    }

    @Test
    fun `select by index activates the requested entry`() {
        val comboBox = comboBox()
        comboBox.selectedIndex = 0

        val outcome = ComboBoxInteractor.select(comboBox, ItemSelector(index = 2))

        assertTrue(outcome.success)
        assertEquals(2, comboBox.selectedIndex)
        assertEquals("Blue", outcome.matchedItem?.text)
        assertTrue(outcome.matchedItem?.selected == true)
        assertEquals(listOf("Blue"), outcome.selectionAfter.map { it.text })
    }

    @Test
    fun `select by text activates the matching entry`() {
        val comboBox = comboBox()
        comboBox.selectedIndex = 0

        val outcome = ComboBoxInteractor.select(comboBox, ItemSelector(text = "Green"))

        assertTrue(outcome.success)
        assertEquals(1, comboBox.selectedIndex)
        assertEquals(1, outcome.matchedItem?.index)
    }

    @Test
    fun `select returns notFound and leaves selection untouched when nothing matches`() {
        val comboBox = comboBox()
        comboBox.selectedIndex = 0

        val outcome = ComboBoxInteractor.select(comboBox, ItemSelector(text = "Purple"))

        assertFalse(outcome.success)
        assertNull(outcome.matchedItem)
        assertTrue(outcome.warnings.isNotEmpty())
        assertEquals(0, comboBox.selectedIndex)
    }

    @Test
    fun `itemBounds is always null because popup items have no stable bounds`() {
        val comboBox = comboBox()

        assertNull(ComboBoxInteractor.itemBounds(comboBox, ItemSelector(index = 0)))
        assertNull(ComboBoxInteractor.itemBounds(comboBox, ItemSelector(text = "Red")))
    }
}
