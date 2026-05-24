package com.github.xepozz.ide.introspector.core.platform

import com.github.xepozz.ide.introspector.core.ListenerInspector
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assume.assumeTrue

/**
 * Platform-level smoke tests for [ListenerInspector].
 *
 * Runs against the real test IDE: asserts the reflective walk through
 * `IdeaPluginDescriptorImpl.app/project` → `ContainerDescriptor.listeners` →
 * `ListenerDescriptor.{topicClassName, listenerClassName, …}` actually produces
 * data. If any of those package-private field names drift across platform versions,
 * these tests are the first place the regression surfaces.
 *
 * We assume the bundled platform plugin (`com.intellij`) declares at least one
 * MessageBus listener — historically this includes `FileEditorManagerListener`,
 * `BulkFileListener`, `DumbService` topics, etc. — but we don't pin a specific
 * topic name; only the *presence* of platform-declared listeners is asserted.
 */
class ListenerInspectorPlatformTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        ListenerInspector.getInstance().refresh()
    }

    fun testListenersListIsNonEmpty() {
        val listeners = ListenerInspector.getInstance().list()
        assertFalse(
            "list() must return at least one listener in the test IDE — drift in the " +
                "ContainerDescriptor field names is the most likely cause if this fails",
            listeners.isEmpty(),
        )
    }

    fun testListenersIncludePlatformContribution() {
        val listeners = ListenerInspector.getInstance().list()
        val fromPlatform = listeners.filter { it.providedByPluginId.startsWith("com.intellij") }
        assertFalse(
            "Expected at least one listener attributed to a com.intellij plugin; got pluginIds=" +
                listeners.map { it.providedByPluginId }.distinct(),
            fromPlatform.isEmpty(),
        )
    }

    fun testListenerScopesAreApplicationOrProject() {
        val listeners = ListenerInspector.getInstance().list()
        for (l in listeners) {
            assertTrue(
                "Listener ${l.topicClass} → ${l.listenerClass} has unexpected scope=${l.scope}; " +
                    "must be 'application' or 'project'",
                l.scope == "application" || l.scope == "project",
            )
        }
    }

    fun testAtLeastOneApplicationScopeListenerExists() {
        val listeners = ListenerInspector.getInstance().list()
        val appScope = listeners.filter { it.scope == "application" }
        assumeTrue(
            "Test IDE has no application-scope listeners at all — unusual but skip rather than fail",
            listeners.isNotEmpty(),
        )
        assertFalse(
            "Expected at least one application-scope listener; got scopes=" +
                listeners.map { it.scope }.distinct(),
            appScope.isEmpty(),
        )
    }

    fun testListenerInfoFieldsArePopulated() {
        val listeners = ListenerInspector.getInstance().list()
        assumeTrue("Need at least one listener to inspect field population", listeners.isNotEmpty())
        for (l in listeners.take(50)) {
            assertTrue(
                "topicClass must be a non-blank FQN, got '${l.topicClass}'",
                l.topicClass.isNotBlank(),
            )
            assertTrue(
                "listenerClass must be a non-blank FQN, got '${l.listenerClass}'",
                l.listenerClass.isNotBlank(),
            )
            assertTrue(
                "providedByPluginId must be non-blank, got '${l.providedByPluginId}'",
                l.providedByPluginId.isNotBlank(),
            )
        }
    }

    fun testCacheReturnsSameInstanceWithinTtl() {
        val inspector = ListenerInspector.getInstance()
        val first = inspector.list()
        val second = inspector.list()
        assertSame(
            "Two consecutive list() calls must return the same cached list within TTL",
            first,
            second,
        )
    }

    fun testRefreshInvalidatesCache() {
        val inspector = ListenerInspector.getInstance()
        val before = inspector.list()
        inspector.refresh()
        val after = inspector.list()
        assertNotSame(
            "refresh() must clear the TTL cache so list() rebuilds a fresh list instance",
            before,
            after,
        )
        // Content equality is still expected — plugins didn't change between calls.
        assertEquals(
            "Refreshing the cache must not change listener content when no plugins reloaded",
            before.size,
            after.size,
        )
    }
}
