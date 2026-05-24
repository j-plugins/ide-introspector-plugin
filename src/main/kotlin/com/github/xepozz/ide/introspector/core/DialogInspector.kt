package com.github.xepozz.ide.introspector.core

import com.github.xepozz.ide.introspector.model.Bounds
import com.github.xepozz.ide.introspector.model.DialogInfo
import com.github.xepozz.ide.introspector.model.DialogsResponse
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.DialogWrapper
import java.awt.Dialog
import java.awt.Window

/**
 * Enumerates every live AWT [Dialog] (both `JDialog` and bare `java.awt.Dialog`) and
 * resolves the contributing [DialogWrapper] class where possible.
 *
 * Each returned [DialogInfo] carries a [ComponentRegistry] id that callers can reuse with
 * `ui.get_properties`, `ui.get_tree` (`rootSelector="dialog"`), and
 * `screenshot.capture(target='component')`. Ids are stable within one IDE session — same
 * dialog → same id across calls — because [ComponentRegistry] keys on component identity.
 *
 * EDT-only. Callers must wrap [listDialogs] in `onEdtBlocking { … }`.
 */
object DialogInspector {

    fun listDialogs(includeInvisible: Boolean): DialogsResponse {
        assertEdt()
        val registry = ComponentRegistry.getInstance()
        val warnings = mutableListOf<String>()
        val windows: Array<Window> = runCatching { Window.getWindows() }.getOrElse {
            warnings.add("Window.getWindows() failed: ${it.javaClass.simpleName}: ${it.message ?: "no message"}")
            emptyArray()
        }
        val items = windows.mapNotNull { w ->
            val dlg = w as? Dialog ?: return@mapNotNull null
            val showing = runCatching { dlg.isShowing }.getOrDefault(false)
            if (!includeInvisible && !showing) return@mapNotNull null

            runCatching {
                // Registering ANY dialog (including invisible ones we still surface when
                // includeInvisible=true) is intentional — agents follow up with
                // `ui.get_properties` / screenshot ops keyed on this id. The registry's
                // WeakHashMap drops entries once the dialog is GC'd, so we don't leak.
                val id = registry.register(dlg)
                val b = runCatching { dlg.bounds }.getOrNull()
                val content = runCatching {
                    // findInstance walks UP from the component until it finds a
                    // DialogWrapperDialog. For most IntelliJ dialogs the JDialog itself
                    // already IS the wrapper dialog, so the first iteration matches.
                    // For bare java.awt.Dialog instances this returns null and we fall
                    // back to the dialog's own class name.
                    DialogWrapper.findInstance(dlg)?.javaClass?.name
                }.getOrNull() ?: dlg.javaClass.name

                DialogInfo(
                    id = id,
                    title = runCatching { dlg.title }.getOrNull()?.takeIf { it.isNotEmpty() },
                    isModal = runCatching { dlg.isModal }.getOrDefault(false),
                    isResizable = runCatching { dlg.isResizable }.getOrDefault(false),
                    isShowing = showing,
                    bounds = b?.let { Bounds(it.x, it.y, it.width, it.height) }
                        ?: Bounds(0, 0, 0, 0).also {
                            warnings.add("dialog '${dlg.javaClass.name}': bounds unavailable, emitting zero rect")
                        },
                    contentClass = content,
                )
            }.getOrElse { t ->
                warnings.add("dialog '${dlg.javaClass.name}': ${t.javaClass.simpleName}: ${t.message ?: "no message"}")
                null
            }
        }
        return DialogsResponse(dialogs = items, warnings = warnings)
    }

    /** See note on [ToolWindowInspector.assertEdt] — soft check, not a throw. */
    private fun assertEdt() {
        ApplicationManager.getApplication().isDispatchThread
        // Intentionally a no-op outside diagnostics — the tool wrapper already enforces EDT
        // via onEdtBlocking. Throwing here would break unit tests that exercise the
        // inspector logic on the test thread without going through the MCP machinery.
    }
}
