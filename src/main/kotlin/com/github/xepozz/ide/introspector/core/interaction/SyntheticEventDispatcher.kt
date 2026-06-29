package com.github.xepozz.ide.introspector.core.interaction

import com.intellij.openapi.diagnostic.thisLogger
import java.awt.Component
import java.awt.Point
import java.awt.event.MouseEvent

object SyntheticEventDispatcher {
    fun click(component: Component, point: Point, clickCount: Int, button: MouseButton): Boolean {
        return try {
            for (currentClick in 1..clickCount) {
                dispatchClickCycle(
                    component = component,
                    point = point,
                    clickCount = currentClick,
                    button = button,
                )
            }
            true
        } catch (exception: Exception) {
            thisLogger().debug("Synthetic click dispatch failed on ${component.javaClass.name}", exception)
            false
        }
    }

    private fun dispatchClickCycle(
        component: Component,
        point: Point,
        clickCount: Int,
        button: MouseButton,
    ) {
        dispatchMouseEvent(
            component = component,
            eventId = MouseEvent.MOUSE_PRESSED,
            point = point,
            clickCount = clickCount,
            button = button,
            modifiers = button.downMask,
            popupTrigger = button.triggersPopup,
        )
        dispatchMouseEvent(
            component = component,
            eventId = MouseEvent.MOUSE_RELEASED,
            point = point,
            clickCount = clickCount,
            button = button,
            modifiers = button.downMask,
            popupTrigger = button.triggersPopup,
        )
        dispatchMouseEvent(
            component = component,
            eventId = MouseEvent.MOUSE_CLICKED,
            point = point,
            clickCount = clickCount,
            button = button,
            modifiers = 0,
            popupTrigger = false,
        )
    }

    private fun dispatchMouseEvent(
        component: Component,
        eventId: Int,
        point: Point,
        clickCount: Int,
        button: MouseButton,
        modifiers: Int,
        popupTrigger: Boolean,
    ) {
        component.dispatchEvent(
            MouseEvent(
                component,
                eventId,
                System.currentTimeMillis(),
                modifiers,
                point.x,
                point.y,
                clickCount,
                popupTrigger,
                button.awtButton,
            )
        )
    }
}

enum class MouseButton(
    val awtButton: Int,
    val downMask: Int,
    val triggersPopup: Boolean,
) {
    LEFT(MouseEvent.BUTTON1, MouseEvent.BUTTON1_DOWN_MASK, false),
    MIDDLE(MouseEvent.BUTTON2, MouseEvent.BUTTON2_DOWN_MASK, false),
    RIGHT(MouseEvent.BUTTON3, MouseEvent.BUTTON3_DOWN_MASK, true),
    ;

    companion object {
        fun from(name: String): MouseButton =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: LEFT
    }
}
