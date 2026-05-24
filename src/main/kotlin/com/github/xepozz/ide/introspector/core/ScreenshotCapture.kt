package com.github.xepozz.ide.introspector.core

import com.intellij.openapi.wm.WindowManager
import java.awt.Component
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.Robot
import java.awt.image.BufferedImage

/**
 * Renders a [Component] off-screen via Component.paint(Graphics) — pure rendering, no
 * popups/tooltips. Must be called on the EDT.
 */
object ScreenshotCapture {

    fun captureComponent(component: Component): BufferedImage {
        val w = kotlin.math.max(1, component.width)
        val h = kotlin.math.max(1, component.height)
        val img = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
        val g: Graphics2D = img.createGraphics()
        try {
            component.paint(g)
        } finally {
            g.dispose()
        }
        return img
    }

    /** Real screen capture via Robot — includes popups/tooltips/overlays. */
    fun captureRect(rect: Rectangle): BufferedImage {
        val robot = Robot()
        return robot.createScreenCapture(rect)
    }

    /** Captures the active project frame on EDT. */
    fun captureActiveFrame(): BufferedImage? {
        val frame = WindowManager.getInstance().findVisibleFrame() ?: return null
        return captureComponent(frame)
    }
}
