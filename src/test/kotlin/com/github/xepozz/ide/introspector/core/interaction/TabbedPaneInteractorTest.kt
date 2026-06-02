package com.github.xepozz.ide.introspector.core.interaction

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeFalse
import org.junit.Test
import java.awt.GraphicsEnvironment
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTabbedPane

class TabbedPaneInteractorTest {

    private fun tabbedPane(): JTabbedPane =
        JTabbedPane().apply {
            addTab("Overview", JPanel())
            addTab("Details", JLabel())
            addTab("Settings", JPanel())
        }

    @Test
    fun `supports recognizes a JTabbedPane and rejects a plain panel`() {
        assertTrue(TabbedPaneInteractor.supports(JTabbedPane()))
        assertFalse(TabbedPaneInteractor.supports(JPanel()))
    }

    @Test
    fun `listItems mirrors every tab title selection and enabled state`() {
        val tabbedPane = tabbedPane()
        tabbedPane.selectedIndex = 1
        tabbedPane.setEnabledAt(2, false)

        val items = TabbedPaneInteractor.listItems(tabbedPane)

        assertEquals(3, items.size)
        assertEquals(listOf("Overview", "Details", "Settings"), items.map { it.text })
        assertEquals(listOf(0, 1, 2), items.map { it.index })
        assertEquals(listOf(false, true, false), items.map { it.selected })
        assertEquals(listOf(true, true, false), items.map { it.enabled })
    }

    @Test
    fun `select by index activates the requested tab`() {
        val tabbedPane = tabbedPane()
        tabbedPane.selectedIndex = 0

        val outcome = TabbedPaneInteractor.select(tabbedPane, ItemSelector(index = 2))

        assertTrue(outcome.success)
        assertEquals(2, tabbedPane.selectedIndex)
        assertEquals("Settings", outcome.matchedItem?.text)
        assertTrue(outcome.matchedItem?.selected == true)
        assertEquals(listOf("Settings"), outcome.selectionAfter.map { it.text })
    }

    @Test
    fun `select by title text activates the matching tab`() {
        val tabbedPane = tabbedPane()
        tabbedPane.selectedIndex = 0

        val outcome = TabbedPaneInteractor.select(tabbedPane, ItemSelector(text = "Details"))

        assertTrue(outcome.success)
        assertEquals(1, tabbedPane.selectedIndex)
        assertEquals(1, outcome.matchedItem?.index)
    }

    @Test
    fun `select returns notFound and leaves selection untouched when nothing matches`() {
        val tabbedPane = tabbedPane()
        tabbedPane.selectedIndex = 0

        val outcome = TabbedPaneInteractor.select(tabbedPane, ItemSelector(text = "Absent"))

        assertFalse(outcome.success)
        assertNull(outcome.matchedItem)
        assertTrue(outcome.warnings.isNotEmpty())
        assertEquals(0, tabbedPane.selectedIndex)
    }

    @Test
    fun `select out-of-range index returns notFound`() {
        val tabbedPane = tabbedPane()

        val outcome = TabbedPaneInteractor.select(tabbedPane, ItemSelector(index = 9))

        assertFalse(outcome.success)
    }

    @Test
    fun `itemBounds reports a non-null laid-out rectangle for an existing tab`() {
        assumeFalse("Requires display server to lay out tabs.", GraphicsEnvironment.isHeadless())
        val tabbedPane = tabbedPane()
        tabbedPane.setSize(400, 300)
        tabbedPane.doLayout()

        val bounds = TabbedPaneInteractor.itemBounds(tabbedPane, ItemSelector(index = 0))

        assertNotNull(bounds)
    }

    @Test
    fun `itemBounds returns null for a selector that matches no tab`() {
        val tabbedPane = tabbedPane()

        assertNull(TabbedPaneInteractor.itemBounds(tabbedPane, ItemSelector(text = "Absent")))
    }
}
