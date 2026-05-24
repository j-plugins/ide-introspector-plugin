package com.github.xepozz.ide.introspector.core

import com.intellij.lang.annotation.HighlightSeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Pure-Kotlin unit tests for [EditorStateInspector] helpers that don't need an IDE fixture.
 *
 * Editor / daemon / folding bound tests live in
 * `core/platform/EditorStateInspectorPlatformTest` since they require a real `EditorImpl`.
 */
class EditorStateInspectorTest {

    // ---- parseSeverity -------------------------------------------------------

    @Test
    fun parseSeverityErrorMapsToError() {
        assertEquals(HighlightSeverity.ERROR, EditorStateInspector.parseSeverity("ERROR"))
        assertEquals(HighlightSeverity.ERROR, EditorStateInspector.parseSeverity("error"))
    }

    @Test
    fun parseSeverityWarningMapsToWarning() {
        assertEquals(HighlightSeverity.WARNING, EditorStateInspector.parseSeverity("WARNING"))
    }

    @Test
    fun parseSeverityWeakWarningMapsToWeakWarning() {
        assertEquals(HighlightSeverity.WEAK_WARNING, EditorStateInspector.parseSeverity("WEAK_WARNING"))
    }

    @Test
    fun parseSeverityInfoAndAllMapToInformation() {
        assertEquals(HighlightSeverity.INFORMATION, EditorStateInspector.parseSeverity("INFO"))
        assertEquals(HighlightSeverity.INFORMATION, EditorStateInspector.parseSeverity("INFORMATION"))
        assertEquals(HighlightSeverity.INFORMATION, EditorStateInspector.parseSeverity("ALL"))
        assertEquals(HighlightSeverity.INFORMATION, EditorStateInspector.parseSeverity("all"))
    }

    @Test
    fun parseSeverityUnknownThrows() {
        val ex = assertThrows(IllegalArgumentException::class.java) {
            EditorStateInspector.parseSeverity("HOT_PINK")
        }
        // Message names the allowed values so callers know how to fix it.
        val msg = ex.message ?: ""
        assertEquals(
            "expected error message to mention allowed severities and the bad value",
            true,
            msg.contains("ERROR") && msg.contains("WARNING") && msg.contains("HOT_PINK"),
        )
    }
}
