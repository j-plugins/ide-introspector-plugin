package com.github.xepozz.ide.introspector.core

import com.github.xepozz.ide.introspector.model.ServiceInfo
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.extensions.PluginDescriptor

/**
 * Enumerates IntelliJ [com.intellij.openapi.components.ServiceDescriptor]s reachable from the
 * loaded [PluginDescriptor]s. The descriptor POJOs are pure data carriers — we never call
 * `project.getService(...)` or `componentManager.getServiceIfCreated(...)`, so a broken
 * third-party service `<init>` cannot trip us. Mirrors [ExtensionPointInspector] in its
 * reflection-only approach: never casts to `IdeaPluginDescriptorImpl` (internal API).
 */
object ServiceInventory {

    /** Returns all services across the given [scope] ("application"|"project"|"module"|"all"). */
    fun listServices(scope: String = "all"): List<ServiceInfo> {
        val out = mutableListOf<ServiceInfo>()
        @Suppress("UnstableApiUsage")
        for (descriptor in PluginManagerCore.plugins) {
            out += collectFromPlugin(descriptor, scope)
        }
        return out
    }

    private fun collectFromPlugin(descriptor: PluginDescriptor, scope: String): List<ServiceInfo> {
        val out = mutableListOf<ServiceInfo>()
        val pluginId = descriptor.pluginId?.idString ?: "unknown"
        val pluginName = try { descriptor.name } catch (_: Throwable) { null }

        if (scope == "application" || scope == "all") {
            out += runCatching { collectScope(descriptor, "appContainerDescriptor", "application", pluginId, pluginName) }
                .getOrElse { emptyList() }
        }
        if (scope == "project" || scope == "all") {
            out += runCatching { collectScope(descriptor, "projectContainerDescriptor", "project", pluginId, pluginName) }
                .getOrElse { emptyList() }
        }
        if (scope == "module" || scope == "all") {
            out += runCatching { collectScope(descriptor, "moduleContainerDescriptor", "module", pluginId, pluginName) }
                .getOrElse { emptyList() }
        }
        return out
    }

    private fun collectScope(
        descriptor: Any,
        containerProperty: String,
        scopeTag: String,
        pluginId: String,
        pluginName: String?,
    ): List<ServiceInfo> {
        val container = readContainerDescriptor(descriptor, containerProperty) ?: return emptyList()
        val descriptors = readServicesList(container) ?: return emptyList()
        val out = mutableListOf<ServiceInfo>()
        for (sd in descriptors) {
            if (sd == null) continue
            val info = try {
                serviceInfoOf(sd, scopeTag, pluginId, pluginName)
            } catch (t: Throwable) {
                thisLogger().debug("Failed to inspect service descriptor on plugin $pluginId", t)
                null
            }
            if (info != null) out.add(info)
        }
        return out
    }

    internal fun readContainerDescriptor(descriptor: Any, name: String): Any? {
        // Try a getter first (Kotlin property accessor) then a JVM field — the property is
        // package-private on some platform builds and public on others.
        val getterName = "get" + name.replaceFirstChar { it.uppercase() }
        readMethod(descriptor, getterName)?.let { return it }
        readMethod(descriptor, name)?.let { return it }
        return readField(descriptor, name)
    }

    @Suppress("UNCHECKED_CAST")
    internal fun readServicesList(container: Any): List<Any?>? {
        // `services` is a public List<ServiceDescriptor> field on ContainerDescriptor.
        val viaGetter = readMethod(container, "getServices")
        if (viaGetter is List<*>) return viaGetter as List<Any?>
        val viaField = readField(container, "services")
        if (viaField is List<*>) return viaField as List<Any?>
        return null
    }

    internal fun serviceInfoOf(
        descriptor: Any,
        scopeTag: String,
        pluginId: String,
        pluginName: String?,
    ): ServiceInfo? {
        val impl = readStringMember(descriptor, "serviceImplementation")
        if (impl.isNullOrBlank()) {
            // Malformed declaration — skip silently per plan edge case 2.
            return null
        }
        val iface = readStringMember(descriptor, "serviceInterface").takeUnless { it.isNullOrBlank() } ?: impl
        val testImpl = readStringMember(descriptor, "testServiceImplementation").takeUnless { it.isNullOrBlank() }
        val headlessImpl = readStringMember(descriptor, "headlessImplementation").takeUnless { it.isNullOrBlank() }
        val overrides = readBoolMember(descriptor, "overrides")
        val preload = readPreloadMode(descriptor)
        return ServiceInfo(
            serviceInterface = iface,
            serviceImplementation = impl,
            scope = scopeTag,
            preload = preload,
            overrides = overrides,
            testServiceImplementation = testImpl,
            headlessImplementation = headlessImpl,
            providedByPluginId = pluginId,
            providedByPluginName = pluginName,
        )
    }

    /** Reads a String field/property — handles getter, then plain field. */
    internal fun readStringMember(target: Any, name: String): String? {
        val getterName = "get" + name.replaceFirstChar { it.uppercase() }
        val viaGetter = readMethod(target, getterName)
        if (viaGetter is String) return viaGetter
        val viaField = readField(target, name)
        return viaField as? String
    }

    internal fun readBoolMember(target: Any, name: String): Boolean {
        val getterName = "is" + name.replaceFirstChar { it.uppercase() }
        val viaGetter = readMethod(target, getterName)
            ?: readMethod(target, "get" + name.replaceFirstChar { it.uppercase() })
        if (viaGetter is Boolean) return viaGetter
        val viaField = readField(target, name)
        return viaField as? Boolean ?: false
    }

    /**
     * Reads `preload` as either a [com.intellij.openapi.components.PreloadMode] enum (modern
     * builds — surface its `.name`) or a [Boolean] (older builds — map to "TRUE"/"FALSE").
     * Defaults to "FALSE" if absent or unreadable.
     */
    internal fun readPreloadMode(descriptor: Any): String {
        val getter = readMethod(descriptor, "getPreload") ?: readField(descriptor, "preload")
        return when (getter) {
            null -> "FALSE"
            is Boolean -> if (getter) "TRUE" else "FALSE"
            is Enum<*> -> getter.name
            else -> {
                // Reflectively read .name in case the enum class is shadowed across classloaders.
                val nameValue = try {
                    getter.javaClass.methods.firstOrNull { it.name == "name" && it.parameterCount == 0 }
                        ?.invoke(getter)
                        ?.toString()
                } catch (_: Throwable) {
                    null
                }
                nameValue ?: getter.toString()
            }
        }
    }

    internal fun readMethod(target: Any, name: String): Any? = try {
        val m = target.javaClass.methods.firstOrNull { it.name == name && it.parameterCount == 0 }
        m?.invoke(target)
    } catch (_: Throwable) {
        null
    }

    /** Walks the class hierarchy looking for a field, honoring superclasses. */
    internal fun readField(target: Any, name: String): Any? {
        var c: Class<*>? = target.javaClass
        while (c != null) {
            val f = c.declaredFields.firstOrNull { it.name == name }
            if (f != null) {
                return try {
                    f.isAccessible = true
                    f.get(target)
                } catch (_: Throwable) {
                    null
                }
            }
            c = c.superclass
        }
        return null
    }
}
