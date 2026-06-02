package com.github.xepozz.ide.introspector.core.interaction

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.awt.Point
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JPanel

class SyntheticEventDispatcherTest {

    private class RecordingMouseListener : MouseListener {
        val pressed = mutableListOf<MouseEvent>()
        val released = mutableListOf<MouseEvent>()
        val clicked = mutableListOf<MouseEvent>()

        override fun mousePressed(event: MouseEvent) {
            pressed.add(event)
        }

        override fun mouseReleased(event: MouseEvent) {
            released.add(event)
        }

        override fun mouseClicked(event: MouseEvent) {
            clicked.add(event)
        }

        override fun mouseEntered(event: MouseEvent) {}

        override fun mouseExited(event: MouseEvent) {}
    }

    @Test
    fun `single click delivers exactly one pressed released clicked cycle`() {
        val panel = JPanel()
        val listener = RecordingMouseListener()
        panel.addMouseListener(listener)

        val dispatched = SyntheticEventDispatcher.click(
            component = panel,
            point = Point(7, 13),
            clickCount = 1,
            button = MouseButton.LEFT,
        )

        assertTrue(dispatched)
        assertEquals(listOf(1), listener.pressed.map { it.clickCount })
        assertEquals(listOf(1), listener.released.map { it.clickCount })
        assertEquals(listOf(1), listener.clicked.map { it.clickCount })
    }

    @Test
    fun `double click delivers two progressive cycles with incrementing click count`() {
        val panel = JPanel()
        val listener = RecordingMouseListener()
        panel.addMouseListener(listener)

        SyntheticEventDispatcher.click(
            component = panel,
            point = Point(42, 99),
            clickCount = 2,
            button = MouseButton.LEFT,
        )

        assertEquals(listOf(1, 2), listener.pressed.map { it.clickCount })
        assertEquals(listOf(1, 2), listener.released.map { it.clickCount })
        assertEquals(listOf(1, 2), listener.clicked.map { it.clickCount })
    }

    @Test
    fun `click reports source point on every event`() {
        val panel = JPanel()
        val listener = RecordingMouseListener()
        panel.addMouseListener(listener)

        SyntheticEventDispatcher.click(
            component = panel,
            point = Point(42, 99),
            clickCount = 1,
            button = MouseButton.LEFT,
        )

        val events = listOf(listener.pressed, listener.released, listener.clicked).map { it.single() }
        events.forEach { event ->
            assertSame(panel, event.source)
            assertEquals(42, event.x)
            assertEquals(99, event.y)
        }
    }

    @Test
    fun `left click uses button one and is never a popup trigger`() {
        val panel = JPanel()
        val listener = RecordingMouseListener()
        panel.addMouseListener(listener)

        SyntheticEventDispatcher.click(
            component = panel,
            point = Point(1, 1),
            clickCount = 1,
            button = MouseButton.LEFT,
        )

        assertEquals(MouseEvent.BUTTON1, listener.pressed.single().button)
        assertFalse(listener.pressed.single().isPopupTrigger)
        assertFalse(listener.released.single().isPopupTrigger)
        assertFalse(listener.clicked.single().isPopupTrigger)
    }

    @Test
    fun `middle click uses button two and is never a popup trigger`() {
        val panel = JPanel()
        val listener = RecordingMouseListener()
        panel.addMouseListener(listener)

        SyntheticEventDispatcher.click(
            component = panel,
            point = Point(1, 1),
            clickCount = 1,
            button = MouseButton.MIDDLE,
        )

        assertEquals(MouseEvent.BUTTON2, listener.pressed.single().button)
        assertFalse(listener.pressed.single().isPopupTrigger)
        assertFalse(listener.released.single().isPopupTrigger)
        assertFalse(listener.clicked.single().isPopupTrigger)
    }

    @Test
    fun `right click uses button three and triggers popup on press and release only`() {
        val panel = JPanel()
        val listener = RecordingMouseListener()
        panel.addMouseListener(listener)

        SyntheticEventDispatcher.click(
            component = panel,
            point = Point(1, 1),
            clickCount = 1,
            button = MouseButton.RIGHT,
        )

        assertEquals(MouseEvent.BUTTON3, listener.pressed.single().button)
        assertTrue(listener.pressed.single().isPopupTrigger)
        assertTrue(listener.released.single().isPopupTrigger)
        assertFalse(listener.clicked.single().isPopupTrigger)
    }

    @Test
    fun `press and release carry the matching button down mask`() {
        val panel = JPanel()
        val listener = RecordingMouseListener()
        panel.addMouseListener(listener)

        SyntheticEventDispatcher.click(
            component = panel,
            point = Point(1, 1),
            clickCount = 1,
            button = MouseButton.RIGHT,
        )

        assertTrue(listener.pressed.single().modifiersEx and MouseEvent.BUTTON3_DOWN_MASK != 0)
        assertTrue(listener.released.single().modifiersEx and MouseEvent.BUTTON3_DOWN_MASK != 0)
    }

    @Test
    fun `from maps known names case insensitively`() {
        assertEquals(MouseButton.LEFT, MouseButton.from("left"))
        assertEquals(MouseButton.MIDDLE, MouseButton.from("Middle"))
        assertEquals(MouseButton.RIGHT, MouseButton.from("RIGHT"))
    }

    @Test
    fun `from falls back to left for unknown names`() {
        assertEquals(MouseButton.LEFT, MouseButton.from("nonsense"))
        assertEquals(MouseButton.LEFT, MouseButton.from(""))
    }
}
