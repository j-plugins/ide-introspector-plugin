package com.github.xepozz.ide.introspector.core.platform

import com.github.xepozz.ide.introspector.core.PluginInventory
import com.github.xepozz.ide.introspector.core.ServiceInventory
import com.intellij.testFramework.LightPlatformTestCase
import org.junit.Assume.assumeTrue

/**
 * Platform-level smoke tests for [ServiceInventory] and the services arm of [PluginInventory].
 *
 * Verifies that we can enumerate real `ServiceDescriptor`s reachable from
 * `IdeaPluginDescriptorImpl.appContainerDescriptor.services` etc. without instantiating any
 * service. Exact service names vary across IDE editions / versions — we assert on shape and
 * provenance, not on specific FQNs.
 *
 * Each test starts from a forced refresh so behaviour is independent of run order.
 */
class ServiceInventoryPlatformTest : LightPlatformTestCase() {

    override fun setUp() {
        super.setUp()
        PluginInventory.getInstance().refresh()
    }

    fun testServicesListIsNonEmpty() {
        val services = PluginInventory.getInstance().services()
        assertFalse(
            "services() must return at least one entry in the test IDE",
            services.isEmpty(),
        )
    }

    fun testServicesIncludeApplicationScope() {
        val services = PluginInventory.getInstance().services()
        assertTrue(
            "Expected at least one application-scoped service; got scopes=${services.map { it.scope }.distinct()}",
            services.any { it.scope == "application" },
        )
    }

    fun testServicesIncludeProjectScope() {
        val services = PluginInventory.getInstance().services()
        assertTrue(
            "Expected at least one project-scoped service; got scopes=${services.map { it.scope }.distinct()}",
            services.any { it.scope == "project" },
        )
    }

    fun testServicesIncludePlatformProvider() {
        val services = PluginInventory.getInstance().services()
        assertTrue(
            "Expected at least one service provided by com.intellij*",
            services.any { it.providedByPluginId.startsWith("com.intellij") },
        )
    }

    fun testServicesHaveNonBlankInterfaceAndImplementation() {
        val services = PluginInventory.getInstance().services()
        for (s in services) {
            assertTrue(
                "serviceInterface must be non-blank for $s",
                s.serviceInterface.isNotBlank(),
            )
            assertTrue(
                "serviceImplementation must be non-blank for $s",
                s.serviceImplementation.isNotBlank(),
            )
        }
    }

    fun testServicesByPluginFiltersByPluginId() {
        val inventory = PluginInventory.getInstance()
        val candidate = inventory.services()
            .groupingBy { it.providedByPluginId }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
        assumeTrue("Need at least one service provider to test filtering", candidate != null)
        val filtered = inventory.servicesByPlugin(candidate!!)
        assertFalse(
            "servicesByPlugin($candidate) must not be empty for a known provider",
            filtered.isEmpty(),
        )
        for (s in filtered) {
            assertEquals(
                "Every service returned must have providedByPluginId == $candidate",
                candidate,
                s.providedByPluginId,
            )
        }
    }

    fun testCacheRefreshProducesFreshServicesList() {
        val inventory = PluginInventory.getInstance()
        val before = inventory.snapshot()
        inventory.refresh()
        val after = inventory.snapshot()
        assertNotSame(
            "refresh() must invalidate the cache so subsequent snapshot() exposes a new services list",
            before.services,
            after.services,
        )
    }

    fun testListServicesDirectMirrorsCache() {
        val direct = ServiceInventory.listServices("all")
        val cached = PluginInventory.getInstance().services()
        // Both come from the same enumeration path; counts must match.
        assertEquals(
            "Direct ServiceInventory.listServices and cached PluginInventory.services() must agree on count",
            direct.size,
            cached.size,
        )
    }

    fun testScopeFilterApplicationOnly() {
        val applicationOnly = ServiceInventory.listServices("application")
        assertFalse("application-scope list should be non-empty", applicationOnly.isEmpty())
        for (s in applicationOnly) {
            assertEquals(
                "Only application-scope services expected, got ${s.scope}",
                "application",
                s.scope,
            )
        }
    }
}
