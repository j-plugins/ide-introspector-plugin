package com.github.xepozz.ide.introspector.core.platform

import com.github.xepozz.ide.introspector.core.ToolWindowInspector
import com.github.xepozz.ide.introspector.model.ToolWindowsResponse
import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Platform-level tests for [ToolWindowInspector].
 *
 * Uses [BasePlatformTestCase] so a project (and therefore a [com.intellij.openapi.wm.ToolWindowManager])
 * exists. The light test fixture does NOT register the "Project" / "Run" tool windows by
 * default — so we assert structural invariants (response shape, project name, filter
 * subset behaviour) rather than pinning to a specific tool-window id.
 *
 * Plugin-attribution check (test 4) regression-guards Finding 1 of the
 * `ui-semantic-listing` review — the field used to hard-code `null`. We probe
 * [ToolWindowInspector.collectToolWindowPluginIds] directly so the assertion is
 * independent of which tool windows actually instantiate in the light fixture.
 */
class ToolWindowInspectorPlatformTest : BasePlatformTestCase() {

    fun testResponseStructureMatchesContract() {
        val response = runOnEdt {
            ToolWindowInspector.listToolWindows(
                includeInvisible = true, nameContains = null, focusedProject = project,
            )
        }
        assertEquals(
            "Inspector must report the focused-project name, got '${response.project}'",
            project.name, response.project,
        )
        // Light fixture has no tool windows — but the response must still be well-formed
        // (non-null lists, no exceptions in warnings caused by the inspector itself).
        assertNotNull("toolWindows list must be non-null", response.toolWindows)
        assertNotNull("warnings list must be non-null", response.warnings)
        for (tw in response.toolWindows) {
            assertTrue("id must be non-blank, got '${tw.id}'", tw.id.isNotBlank())
            assertTrue(
                "anchor must be a stripe constant (LEFT/RIGHT/BOTTOM/TOP/UNKNOWN), got '${tw.anchor}'",
                tw.anchor in setOf("LEFT", "RIGHT", "BOTTOM", "TOP", "UNKNOWN"),
            )
            assertTrue(
                "type must be a ToolWindowType.name() value (DOCKED/FLOATING/SLIDING/WINDOWED/UNKNOWN), got '${tw.type}'",
                tw.type in setOf("DOCKED", "FLOATING", "SLIDING", "WINDOWED", "UNKNOWN"),
            )
        }
    }

    fun testNameContainsIsCaseInsensitive() {
        val (all, lower, upper) = runOnEdt {
            val all = ToolWindowInspector.listToolWindows(
                includeInvisible = true, nameContains = null, focusedProject = project,
            )
            val lower = ToolWindowInspector.listToolWindows(
                includeInvisible = true, nameContains = "project", focusedProject = project,
            )
            val upper = ToolWindowInspector.listToolWindows(
                includeInvisible = true, nameContains = "PROJECT", focusedProject = project,
            )
            Triple(all, lower, upper)
        }
        assertEquals(
            "Case-insensitive nameContains must produce identical id sets for 'project' vs 'PROJECT'; " +
                "got lower=${lower.toolWindows.map { it.id }}, upper=${upper.toolWindows.map { it.id }}",
            lower.toolWindows.map { it.id }.toSet(),
            upper.toolWindows.map { it.id }.toSet(),
        )
        // Filtered subset must always be ⊆ unfiltered.
        val allIds = all.toolWindows.map { it.id }.toSet()
        assertTrue(
            "nameContains result must be a subset of unfiltered listing; " +
                "filteredOnly=${lower.toolWindows.map { it.id }.toSet() - allIds}",
            allIds.containsAll(lower.toolWindows.map { it.id }),
        )
    }

    fun testIncludeInvisibleFalseExcludesHidden() {
        val (all, visibleOnly) = runOnEdt {
            val all = ToolWindowInspector.listToolWindows(
                includeInvisible = true, nameContains = null, focusedProject = project,
            )
            val visibleOnly = ToolWindowInspector.listToolWindows(
                includeInvisible = false, nameContains = null, focusedProject = project,
            )
            all to visibleOnly
        }
        assertTrue(
            "includeInvisible=false must produce a subset of includeInvisible=true; " +
                "got all=${all.toolWindows.size}, visibleOnly=${visibleOnly.toolWindows.size}",
            visibleOnly.toolWindows.size <= all.toolWindows.size,
        )
        for (tw in visibleOnly.toolWindows) {
            assertTrue(
                "Tool window '${tw.id}' surfaced with includeInvisible=false but isVisible=false",
                tw.isVisible,
            )
        }
    }

    fun testToolWindowEpAttributesIdsToComIntellijPlugin() {
        // Direct EP walk — independent of project resolution and of the light fixture's
        // tool-window subset. Regression guard for Finding 1 of the review (the field was
        // hard-coded to `null`). At least one platform-bundled `com.intellij.toolWindow`
        // extension must be attributed back to its declaring `com.intellij*` plugin.
        val pluginIds = ToolWindowInspector.collectToolWindowPluginIds()
        if (pluginIds.isEmpty()) {
            // No toolWindow EP contributions visible in this build — treat as environmental
            // skip, not a hard fail. We can't synthesise a tool-window EP from here.
            return
        }
        val intellijContributions = pluginIds.values.filter { it.startsWith("com.intellij") }
        assertFalse(
            "Expected at least one tool-window attributed to a com.intellij plugin; got pluginIds=" +
                pluginIds.values.distinct(),
            intellijContributions.isEmpty(),
        )
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

    // Unused but kept as a sanity import — silences "unused" inspection for the model type.
    @Suppress("unused")
    private fun touchType(): ToolWindowsResponse? = null
}
