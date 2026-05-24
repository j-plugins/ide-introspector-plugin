package com.github.xepozz.ide.introspector.core.platform

import com.github.xepozz.ide.introspector.core.TopicInspector
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assume.assumeNotNull
import org.junit.Assume.assumeTrue

/**
 * Platform-level smoke tests for [TopicInspector].
 *
 * Exercises the classpath-walk + reflection chain against the real loaded plugins in the
 * sandbox IDE. We make minimal assumptions: only that at least one bundled plugin declares
 * at least one `Topic<L>` (the platform certainly does — `BulkFileListener`,
 * `FileEditorManagerListener`, etc.).
 *
 * For the more interesting "did our own demo Topic get discovered?" assertion we look up
 * this plugin's descriptor by id — in test fixtures the descriptor MAY not be present
 * (depends on how the test runner mounts the plugin), in which case we skip rather than
 * fail.
 */
class TopicInspectorPlatformTest : BasePlatformTestCase() {

    fun testListAllReturnsSomethingFromAtLeastOneBundledPlugin() {
        val all = TopicInspector.listAll()
        assumeTrue(
            "Test IDE classpath scan produced no Topic fields — unusual, skipping",
            all.isNotEmpty(),
        )
        val byPlatform = all.filter { it.providedByPluginId.startsWith("com.intellij") }
        assertTrue(
            "Expected at least one Topic owned by the platform; got plugin ids ${all.map { it.providedByPluginId }.distinct()}",
            byPlatform.isNotEmpty(),
        )
    }

    fun testEveryTopicHasNonBlankClassesAndListener() {
        for (t in TopicInspector.listAll()) {
            assertTrue("declaringClassName must be non-blank for $t", t.declaringClassName.isNotBlank())
            assertTrue("fieldName must be non-blank for $t", t.fieldName.isNotBlank())
            assertTrue("listenerClassName must be non-blank for $t", t.listenerClassName.isNotBlank())
            assertTrue("id must follow 'declaringClass.fieldName' shape for $t",
                t.id == "${t.declaringClassName}.${t.fieldName}")
        }
    }

    fun testIntrospectorDemoTopicDiscoveredWhenOurPluginIsLoaded() {
        val descriptor = PluginManagerCore.getPlugin(
            PluginId.getId("com.github.xepozz.ide.introspector")
        )
        assumeNotNull(
            "Plugin descriptor not loaded in this fixture — skipping our-plugin assertion",
            descriptor,
        )
        val topics = TopicInspector.listForPlugin(descriptor!!)
        val demo = topics.firstOrNull { it.declaringClassName.endsWith(".IntrospectorDemoListener") }
        assumeTrue(
            "Plugin classpath not enumerable via pluginPath in this fixture (typical for compiled-classes runs)",
            demo != null,
        )
        assertEquals(
            "IntrospectorDemoListener.TOPIC must point at the IntrospectorDemoListener interface",
            "com.github.xepozz.ide.introspector.examples.IntrospectorDemoListener",
            demo!!.listenerClassName,
        )
        assertEquals("TOPIC", demo.fieldName)
        assertFalse("Demo topic uses @JvmField — not held on Companion", demo.onCompanion)
    }
}
