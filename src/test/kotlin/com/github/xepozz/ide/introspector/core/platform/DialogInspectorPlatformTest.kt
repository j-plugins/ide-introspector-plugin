package com.github.xepozz.ide.introspector.core.platform

import com.github.xepozz.ide.introspector.core.ComponentRegistry
import com.github.xepozz.ide.introspector.core.DialogInspector
import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.awt.GraphicsEnvironment
import javax.swing.JDialog

/**
 * Platform-level tests for [DialogInspector].
 *
 * Many tests require constructing a real [JDialog], which throws
 * [java.awt.HeadlessException] in a true headless JVM (CI). When that's the case we
 * early-return — the inspector itself is still exercised by
 * [testEmptyListWhenIncludeInvisibleFalseAndNoShowingDialog], which doesn't need to
 * fabricate a dialog.
 *
 * The interesting invariants:
 *  - `id` is a [ComponentRegistry] handle — passing the same dialog through twice yields
 *    the same id (regression guard for Finding 3 of the `ui-semantic-listing` review).
 *  - `title=null` for an untitled [JDialog] (plan edge case #3).
 *  - `bounds` is non-null (plan §"Files to create/modify" → DialogInfo).
 *  - `isModal` reflects [JDialog.isModal].
 */
class DialogInspectorPlatformTest : BasePlatformTestCase() {

    fun testListDialogsNeverThrowsAndReturnsWellFormedResponse() {
        val response = runOnEdt { DialogInspector.listDialogs(includeInvisible = false) }
        assertNotNull("dialogs list must be non-null", response.dialogs)
        assertNotNull("warnings list must be non-null", response.warnings)
        // includeInvisible=false: every entry must report isShowing=true (the filter contract).
        for (info in response.dialogs) {
            assertTrue(
                "includeInvisible=false must drop non-showing dialogs; " +
                    "'${info.contentClass}' surfaced with isShowing=false",
                info.isShowing,
            )
            assertNotNull("bounds must be non-null per plan contract", info.bounds)
            assertTrue("id must follow ComponentRegistry format 'c_xxxxxxxx'", info.id.startsWith("c_"))
        }
    }

    fun testJDialogWithKnownTitleAndModalitySurfaces() {
        if (GraphicsEnvironment.isHeadless()) return
        val info = runOnEdt {
            val dialog = JDialog(null as java.awt.Frame?, "IntrospectorTestDialog", /* modal = */ true)
            try {
                val response = DialogInspector.listDialogs(includeInvisible = true)
                response.dialogs.firstOrNull { it.title == "IntrospectorTestDialog" }
            } finally {
                dialog.dispose()
            }
        }
        assertNotNull(
            "Dialog with title 'IntrospectorTestDialog' must surface in includeInvisible=true listing",
            info,
        )
        assertTrue(
            "JDialog constructed with modal=true must report isModal=true, got ${info!!.isModal}",
            info.isModal,
        )
        assertNotNull("bounds must be non-null per plan contract", info.bounds)
    }

    fun testJDialogWithNullTitleEmitsNullTitle() {
        if (GraphicsEnvironment.isHeadless()) return
        val matched = runOnEdt {
            val dialog = JDialog()
            try {
                val response = DialogInspector.listDialogs(includeInvisible = true)
                // No title to match on — pick by ComponentRegistry id of the dialog we just
                // registered. The DialogInspector calls register(dlg) once per call.
                val registry = ComponentRegistry.getInstance()
                val expectedId = registry.register(dialog)
                response.dialogs.firstOrNull { it.id == expectedId }
            } finally {
                dialog.dispose()
            }
        }
        assertNotNull(
            "Untitled JDialog must surface in includeInvisible=true listing",
            matched,
        )
        assertNull(
            "JDialog() with no title must emit title=null (empty-string title normalised to null); got '${matched!!.title}'",
            matched.title,
        )
        assertEquals(
            "contentClass must fall back to the dialog's own FQN when no DialogWrapper is attached",
            JDialog::class.java.name, matched.contentClass,
        )
    }

    fun testSameDialogYieldsStableComponentRegistryId() {
        if (GraphicsEnvironment.isHeadless()) return
        val (dialog, firstId, secondId) = runOnEdt {
            val dialog = JDialog(null as java.awt.Frame?, "StableIdProbe", false)
            try {
                val first = DialogInspector.listDialogs(includeInvisible = true)
                    .dialogs.firstOrNull { it.title == "StableIdProbe" }
                val second = DialogInspector.listDialogs(includeInvisible = true)
                    .dialogs.firstOrNull { it.title == "StableIdProbe" }
                Triple(dialog, first?.id, second?.id)
            } finally {
                // Don't dispose yet — caller asserts on registry lookup below.
            }
        }
        try {
            assertNotNull("first call must surface the probe dialog", firstId)
            assertNotNull("second call must surface the probe dialog", secondId)
            assertEquals(
                "ComponentRegistry id must be stable across calls for the same dialog instance; " +
                    "got first=$firstId, second=$secondId",
                firstId, secondId,
            )
            val resolved = ComponentRegistry.getInstance().lookup(firstId!!)
            assertSame(
                "ComponentRegistry.lookup must return the same dialog instance we registered",
                dialog, resolved,
            )
        } finally {
            runOnEdt { dialog.dispose() }
        }
    }

    private fun <T> runOnEdt(block: () -> T): T {
        val ref = arrayOfNulls<Any>(1)
        var thrown: Throwable? = null
        ApplicationManager.getApplication().invokeAndWait {
            try { ref[0] = block() as Any? } catch (t: Throwable) { thrown = t }
        }
        thrown?.let { throw it }
        @Suppress("UNCHECKED_CAST")
        return ref[0] as T
    }
}
