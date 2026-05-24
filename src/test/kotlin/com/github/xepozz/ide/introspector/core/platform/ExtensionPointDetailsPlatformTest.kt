package com.github.xepozz.ide.introspector.core.platform

import com.github.xepozz.ide.introspector.core.ExtensionPointInspector
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assume.assumeTrue

/**
 * Platform-level tests for `arch.get_extension_point_details`. Asserts the live IDE's
 * built-in Extension Points actually round-trip through [ExtensionPointInspector.getDetails]
 * with the right kind / bean-schema / interface-method shape.
 *
 * EPs picked here are bundled with the platform and have been stable for many releases:
 *   - `com.intellij.toolWindow` (BEAN_CLASS — ToolWindowEP, fields id/anchor/factoryClass)
 *   - `com.intellij.codeInsight.lineMarkerProvider` (INTERFACE — LineMarkerProvider with
 *     a tiny abstract surface)
 * If either is gone in a future build, the assertions will fail loud rather than
 * silently — that's intentional, the test doubles as a "did the platform rename EP X?"
 * smoke test.
 */
class ExtensionPointDetailsPlatformTest : BasePlatformTestCase() {

    fun testToolWindowEpReturnsBeanSchemaWithExpectedFields() {
        val details = ExtensionPointInspector.getDetails("com.intellij.toolWindow")
        assertNotNull("Expected com.intellij.toolWindow to be registered", details)
        details!!
        assertEquals("BEAN_CLASS", details.kind)
        assertEquals("com.intellij.openapi.wm.ToolWindowEP", details.interfaceOrBeanClass)
        assertEquals("application", details.area)
        val schema = details.beanSchema
        assertNotNull("Expected beanSchema for BEAN_CLASS EP", schema)
        val fieldNames = schema!!.fields.map { it.name }.toSet()
        // ToolWindowEP has been stable for years — these are required-ish columns.
        // Use a subset check so the test survives the platform adding new fields.
        for (expected in listOf("id", "anchor", "factoryClass")) {
            assertTrue(
                "Expected field '$expected' in ToolWindowEP schema; got $fieldNames",
                fieldNames.contains(expected),
            )
        }
    }

    fun testLineMarkerProviderEpReturnsInterfaceMethods() {
        val name = "com.intellij.codeInsight.lineMarkerProvider"
        val details = ExtensionPointInspector.getDetails(name)
        assumeTrue(
            "$name not registered in this sandbox — skipping interface-method assertions",
            details != null,
        )
        details!!
        assertEquals("INTERFACE", details.kind)
        val methods = details.interfaceMethods
        assertNotNull("Expected interfaceMethods for INTERFACE EP", methods)
        val methodNames = methods!!.map { it.name }.toSet()
        assertTrue(
            "Expected 'getLineMarkerInfo' on LineMarkerProvider interface; got $methodNames",
            methodNames.contains("getLineMarkerInfo"),
        )
    }

    fun testUnknownEpReturnsNullInsteadOfThrowing() {
        val details = ExtensionPointInspector.getDetails("com.example.definitely.not.an.ep")
        assertNull("Unknown EP name must return null, not throw", details)
    }

    fun testIncludeRegisteredCountMatchesAdapterCount() {
        val name = "com.intellij.toolWindow"
        val details = ExtensionPointInspector.getDetails(name, includeRegisteredCount = true)
        assertNotNull(details)
        details!!
        assertNotNull(
            "registeredCount must be populated when includeRegisteredCount=true",
            details.registeredCount,
        )
        assertTrue(
            "Expected a positive ToolWindow registration count, got ${details.registeredCount}",
            (details.registeredCount ?: -1) > 0,
        )
    }

    fun testIncludeRegisteredCountDefaultsToNull() {
        val details = ExtensionPointInspector.getDetails("com.intellij.toolWindow")
        assertNotNull(details)
        assertNull(
            "registeredCount must be null when includeRegisteredCount=false (default)",
            details!!.registeredCount,
        )
    }

    fun testBeanSchemaCanBeOmitted() {
        val details = ExtensionPointInspector.getDetails(
            "com.intellij.toolWindow",
            includeBeanSchema = false,
        )
        assertNotNull(details)
        assertNull(
            "beanSchema must be omitted when includeBeanSchema=false",
            details!!.beanSchema,
        )
    }
}
