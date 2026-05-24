package com.github.xepozz.ide.introspector.core

import com.github.xepozz.ide.introspector.model.ServiceInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM reflection tests for [ServiceInventory] — no IntelliJ test fixture needed.
 *
 * The platform tests under `core/platform/` cover the happy paths against a live IDE
 * (real `ServiceDescriptor` / `ContainerDescriptor` shapes). The reflection fallbacks
 * exercised here exist to survive IDE version drift (different field/method names,
 * `PreloadMode` enum vs Boolean), and those branches are unreachable without synthetic
 * doubles.
 *
 * Pattern: feed `serviceInfoOf` / `readContainerDescriptor` / `readServicesList` plain
 * Kotlin objects whose member shape matches what `IdeaPluginDescriptorImpl` /
 * `ContainerDescriptor` / `ServiceDescriptor` expose at the JVM reflection level.
 */
class ServiceInventoryReflectionTest {

    // ====================================================================================
    // serviceInfoOf — happy paths and field fallbacks
    // ====================================================================================

    @Test
    fun `serviceInfoOf reads interface and implementation when both present`() {
        val sd = ServiceDescriptorPojo(
            serviceInterface = "com.example.MyService",
            serviceImplementation = "com.example.MyServiceImpl",
        )
        val info = ServiceInventory.serviceInfoOf(sd, "application", "com.example", "Example")
        assertNotNull(info)
        assertEquals("com.example.MyService", info!!.serviceInterface)
        assertEquals("com.example.MyServiceImpl", info.serviceImplementation)
        assertEquals("application", info.scope)
        assertEquals("com.example", info.providedByPluginId)
        assertEquals("Example", info.providedByPluginName)
    }

    @Test
    fun `serviceInfoOf falls back to implementation when interface is null`() {
        val sd = ServiceDescriptorPojo(
            serviceInterface = null,
            serviceImplementation = "com.example.OnlyImpl",
        )
        val info = ServiceInventory.serviceInfoOf(sd, "project", "p", null)
        assertNotNull(info)
        assertEquals("com.example.OnlyImpl", info!!.serviceInterface)
        assertEquals("com.example.OnlyImpl", info.serviceImplementation)
        assertEquals("project", info.scope)
    }

    @Test
    fun `serviceInfoOf returns null when serviceImplementation is null`() {
        val sd = ServiceDescriptorPojo(
            serviceInterface = "com.example.I",
            serviceImplementation = null,
        )
        val info = ServiceInventory.serviceInfoOf(sd, "application", "p", null)
        assertNull("Descriptor with null implementation must be skipped", info)
    }

    @Test
    fun `serviceInfoOf returns null when serviceImplementation is blank`() {
        val sd = ServiceDescriptorPojo(
            serviceInterface = "com.example.I",
            serviceImplementation = "",
        )
        assertNull(ServiceInventory.serviceInfoOf(sd, "application", "p", null))
    }

    @Test
    fun `serviceInfoOf surfaces preload as PreloadMode dot name when enum present`() {
        val sd = ServiceDescriptorWithPreloadEnum(
            serviceInterface = "com.example.I",
            serviceImplementation = "com.example.Impl",
            preload = FakePreloadMode.TRUE,
        )
        val info = ServiceInventory.serviceInfoOf(sd, "application", "p", null)
        assertEquals("TRUE", info!!.preload)
    }

    @Test
    fun `serviceInfoOf reads preload as Boolean for older platform builds`() {
        val sd = ServiceDescriptorWithBooleanPreload(
            serviceInterface = "com.example.I",
            serviceImplementation = "com.example.Impl",
            preload = true,
        )
        val info = ServiceInventory.serviceInfoOf(sd, "application", "p", null)
        assertEquals("TRUE", info!!.preload)
    }

    @Test
    fun `serviceInfoOf defaults preload to FALSE when neither getter nor field present`() {
        val sd = ServiceDescriptorPojo(
            serviceInterface = "com.example.I",
            serviceImplementation = "com.example.Impl",
        )
        val info = ServiceInventory.serviceInfoOf(sd, "application", "p", null)
        assertEquals("FALSE", info!!.preload)
    }

    @Test
    fun `serviceInfoOf surfaces overrides flag`() {
        val sd = ServiceDescriptorWithOverrides(
            serviceInterface = "com.example.I",
            serviceImplementation = "com.example.Impl",
            overrides = true,
        )
        val info = ServiceInventory.serviceInfoOf(sd, "application", "p", null)
        assertTrue(info!!.overrides)
    }

    @Test
    fun `serviceInfoOf overrides defaults to false`() {
        val sd = ServiceDescriptorPojo(
            serviceInterface = "com.example.I",
            serviceImplementation = "com.example.Impl",
        )
        val info = ServiceInventory.serviceInfoOf(sd, "application", "p", null)
        assertFalse(info!!.overrides)
    }

    @Test
    fun `serviceInfoOf surfaces testServiceImplementation and headlessImplementation`() {
        val sd = ServiceDescriptorWithExtras(
            serviceInterface = "com.example.I",
            serviceImplementation = "com.example.Impl",
            testServiceImplementation = "com.example.TestImpl",
            headlessImplementation = "com.example.HeadlessImpl",
        )
        val info = ServiceInventory.serviceInfoOf(sd, "application", "p", null)
        assertEquals("com.example.TestImpl", info!!.testServiceImplementation)
        assertEquals("com.example.HeadlessImpl", info.headlessImplementation)
    }

    @Test
    fun `serviceInfoOf surfaces null for missing test and headless overrides`() {
        val sd = ServiceDescriptorPojo(
            serviceInterface = "com.example.I",
            serviceImplementation = "com.example.Impl",
        )
        val info = ServiceInventory.serviceInfoOf(sd, "application", "p", null)
        assertNull(info!!.testServiceImplementation)
        assertNull(info.headlessImplementation)
    }

    @Test
    fun `serviceInfoOf treats blank test or headless impl as null`() {
        val sd = ServiceDescriptorWithExtras(
            serviceInterface = "com.example.I",
            serviceImplementation = "com.example.Impl",
            testServiceImplementation = "",
            headlessImplementation = "",
        )
        val info = ServiceInventory.serviceInfoOf(sd, "application", "p", null)
        assertNull(info!!.testServiceImplementation)
        assertNull(info.headlessImplementation)
    }

    @Test
    fun `serviceInfoOf reads interface and impl via Java getters`() {
        // ServiceDescriptor exposes Java-style accessors in some builds; the helper must find
        // them via `getXxx` reflection.
        val sd = ServiceDescriptorWithGetters("com.example.I", "com.example.Impl")
        val info = ServiceInventory.serviceInfoOf(sd, "application", "p", null)
        assertEquals("com.example.I", info!!.serviceInterface)
        assertEquals("com.example.Impl", info.serviceImplementation)
    }

    // ====================================================================================
    // readServicesList — container descriptor surface
    // ====================================================================================

    @Test
    fun `readServicesList reads from a public services field`() {
        val container = ContainerWithServicesField(listOf("s1", "s2"))
        val result = ServiceInventory.readServicesList(container)
        assertNotNull(result)
        assertEquals(2, result!!.size)
    }

    @Test
    fun `readServicesList reads from a getServices method`() {
        val container = ContainerWithGetServicesMethod(listOf("a"))
        val result = ServiceInventory.readServicesList(container)
        assertNotNull(result)
        assertEquals(1, result!!.size)
    }

    @Test
    fun `readServicesList returns null when neither method nor field`() {
        assertNull(ServiceInventory.readServicesList(EmptyContainer()))
    }

    // ====================================================================================
    // readContainerDescriptor — IdeaPluginDescriptorImpl surface
    // ====================================================================================

    @Test
    fun `readContainerDescriptor reads via Kotlin getter`() {
        val target = DescriptorWithGetterAccess()
        val container = ServiceInventory.readContainerDescriptor(target, "appContainerDescriptor")
        assertSame(target.expected, container)
    }

    @Test
    fun `readContainerDescriptor falls back to property-named method`() {
        val target = DescriptorWithKotlinPropertyMethod()
        val container = ServiceInventory.readContainerDescriptor(target, "appContainerDescriptor")
        assertSame(target.expected, container)
    }

    @Test
    fun `readContainerDescriptor falls back to direct field access`() {
        val target = DescriptorWithDirectField()
        val container = ServiceInventory.readContainerDescriptor(target, "appContainerDescriptor")
        assertSame(target.appContainerDescriptor, container)
    }

    @Test
    fun `readContainerDescriptor returns null when neither getter nor field present`() {
        assertNull(ServiceInventory.readContainerDescriptor(EmptyContainer(), "appContainerDescriptor"))
    }

    // ====================================================================================
    // Robustness — bad descriptors don't poison the result
    // ====================================================================================

    @Test
    fun `serviceInfoOf swallows exceptions from a single bad descriptor and returns null`() {
        // The harness invokes serviceInfoOf inside a try/catch; an exception during a getter
        // becomes a null return per readMethod's swallow. Verify the method itself stays sane
        // even if the descriptor's getters throw.
        val sd = DescriptorThatThrowsOnImpl()
        // serviceInfoOf will see a null impl (because readMethod swallowed) and skip.
        val info = ServiceInventory.serviceInfoOf(sd, "application", "p", null)
        assertNull(info)
    }

    // ====================================================================================
    // Filtering — mirror the toolset-level filter behaviour against a synthetic list
    // ====================================================================================

    @Test
    fun `nameContains filter matches against serviceInterface`() {
        val list = listOf(
            svc(iface = "com.example.PsiService", impl = "com.example.Other"),
            svc(iface = "com.example.Foo", impl = "com.example.Foo"),
        )
        val filtered = list.filter { nameContainsMatches(it, "psi") }
        assertEquals(1, filtered.size)
        assertEquals("com.example.PsiService", filtered.single().serviceInterface)
    }

    @Test
    fun `nameContains filter matches against serviceImplementation`() {
        val list = listOf(
            svc(iface = "com.example.Foo", impl = "com.example.PsiImpl"),
            svc(iface = "com.example.Bar", impl = "com.example.BarImpl"),
        )
        val filtered = list.filter { nameContainsMatches(it, "psi") }
        assertEquals(1, filtered.size)
        assertEquals("com.example.PsiImpl", filtered.single().serviceImplementation)
    }

    @Test
    fun `nameContains filter is case-insensitive`() {
        val list = listOf(svc(iface = "com.example.PsiService", impl = "com.example.PsiServiceImpl"))
        assertEquals(1, list.filter { nameContainsMatches(it, "PSISERVICE") }.size)
    }

    @Test
    fun `scope filter accepts matching scope`() {
        val list = listOf(
            svc(scope = "application"),
            svc(scope = "project"),
            svc(scope = "module"),
        )
        assertEquals(1, list.count { it.scope == "application" })
        assertEquals(1, list.count { it.scope == "project" })
        assertEquals(1, list.count { it.scope == "module" })
    }

    @Test
    fun `providedByPluginId filter matches exact id`() {
        val list = listOf(
            svc(pluginId = "com.intellij"),
            svc(pluginId = "org.jetbrains.kotlin"),
            svc(pluginId = "com.intellij"),
        )
        assertEquals(2, list.count { it.providedByPluginId == "com.intellij" })
    }

    @Test
    fun `onlyPreloaded retains services whose preload is not FALSE`() {
        val list = listOf(
            svc(preload = "FALSE"),
            svc(preload = "TRUE"),
            svc(preload = "AWAIT"),
        )
        val filtered = list.filter { it.preload != "FALSE" }
        assertEquals(2, filtered.size)
    }

    private fun svc(
        iface: String = "com.example.I",
        impl: String = "com.example.Impl",
        scope: String = "application",
        preload: String = "FALSE",
        pluginId: String = "com.example",
    ) = ServiceInfo(
        serviceInterface = iface,
        serviceImplementation = impl,
        scope = scope,
        preload = preload,
        providedByPluginId = pluginId,
        providedByPluginName = null,
    )

    private fun nameContainsMatches(svc: ServiceInfo, q: String): Boolean =
        svc.serviceInterface.contains(q, ignoreCase = true) ||
            svc.serviceImplementation.contains(q, ignoreCase = true)

    // ====================================================================================
    // Helpers for assertSame-style comparisons
    // ====================================================================================

    private fun assertSame(expected: Any?, actual: Any?) {
        assertTrue(
            "expected same instance: $expected vs $actual",
            expected === actual,
        )
    }
}

// ========================================================================================
// Fixtures — synthetic ServiceDescriptor-shaped POJOs
// ========================================================================================

private class ServiceDescriptorPojo(
    @JvmField val serviceInterface: String?,
    @JvmField val serviceImplementation: String?,
)

private class ServiceDescriptorWithExtras(
    @JvmField val serviceInterface: String?,
    @JvmField val serviceImplementation: String?,
    @JvmField val testServiceImplementation: String?,
    @JvmField val headlessImplementation: String?,
)

private class ServiceDescriptorWithOverrides(
    @JvmField val serviceInterface: String?,
    @JvmField val serviceImplementation: String?,
    @JvmField val overrides: Boolean,
)

private enum class FakePreloadMode { FALSE, TRUE, NOT_HEADLESS, NOT_LIGHT_EDIT, AWAIT }

private class ServiceDescriptorWithPreloadEnum(
    @JvmField val serviceInterface: String?,
    @JvmField val serviceImplementation: String?,
    @JvmField val preload: FakePreloadMode,
)

private class ServiceDescriptorWithBooleanPreload(
    @JvmField val serviceInterface: String?,
    @JvmField val serviceImplementation: String?,
    @JvmField val preload: Boolean,
)

private class ServiceDescriptorWithGetters(
    private val iface: String,
    private val impl: String,
) {
    fun getServiceInterface(): String = iface
    fun getServiceImplementation(): String = impl
}

private class DescriptorThatThrowsOnImpl {
    @Suppress("unused")
    fun getServiceImplementation(): String = throw IllegalStateException("boom")
}

// ========================================================================================
// Fixtures — container/descriptor shapes
// ========================================================================================

private class ContainerWithServicesField(@JvmField val services: List<Any?>)

private class ContainerWithGetServicesMethod(private val list: List<Any?>) {
    fun getServices(): List<Any?> = list
}

private class EmptyContainer

private class DescriptorWithGetterAccess {
    @JvmField val expected: Any = Any()
    fun getAppContainerDescriptor(): Any = expected
}

private class DescriptorWithKotlinPropertyMethod {
    @JvmField val expected: Any = Any()

    /** Some platform builds expose the container as a plain method matching the field name. */
    @Suppress("unused")
    fun appContainerDescriptor(): Any = expected
}

private class DescriptorWithDirectField {
    @JvmField val appContainerDescriptor: Any = Any()
}
