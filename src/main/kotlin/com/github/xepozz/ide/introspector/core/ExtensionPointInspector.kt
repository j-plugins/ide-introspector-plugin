package com.github.xepozz.ide.introspector.core

import com.github.xepozz.ide.introspector.core.internal.ExtensionMetadata
import com.github.xepozz.ide.introspector.core.internal.PluginDescriptorReader
import com.github.xepozz.ide.introspector.model.ExtensionInfo
import com.github.xepozz.ide.introspector.model.ExtensionPointInfo
import com.github.xepozz.ide.introspector.util.ReflectionAccess
import com.github.xepozz.ide.introspector.util.ReflectionDriftException
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.extensions.ExtensionPoint
import com.intellij.openapi.extensions.ExtensionsArea
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.application.ApplicationManager

/**
 * Reads the live [com.intellij.openapi.extensions.Extensions] graph. EP collection is
 * thread-safe so handlers don't need to bounce on the EDT.
 */
object ExtensionPointInspector {

    /** Returns all EPs in [area] (application/project/both), sorted by name. */
    fun listExtensionPoints(area: String): List<ExtensionPointInfo> {
        val out = mutableListOf<ExtensionPointInfo>()
        if (area == "application" || area == "both") {
            val app = ApplicationManager.getApplication().extensionArea
            out += collectFromArea(app, "application")
        }
        if (area == "project" || area == "both") {
            for (project in ProjectManager.getInstance().openProjects) {
                out += collectFromArea(project.extensionArea, "project")
            }
        }
        return out.sortedBy { it.name }
    }

    private fun collectFromArea(area: ExtensionsArea, areaTag: String): List<ExtensionPointInfo> {
        val out = mutableListOf<ExtensionPointInfo>()
        for (ep in extractAllEps(area)) {
            out += try {
                extensionPointInfoOf(ep, areaTag)
            } catch (t: Throwable) {
                thisLogger().debug("Failed to inspect EP ${epName(ep)}", t)
                null
            } ?: continue
        }
        return out
    }

    /** Reflection-based extraction of all EPs from an [ExtensionsArea]. */
    @Suppress("UNCHECKED_CAST")
    internal fun extractAllEps(area: ExtensionsArea): List<ExtensionPoint<Any>> {
        // ExtensionsAreaImpl exposes a method `getExtensionPoints()` returning Map<String, EP>.
        // We use reflection because it isn't in the public API.
        when (val value = ReflectionAccess.readMethod(area, "getExtensionPoints")) {
            is Map<*, *> -> return value.values.filterIsInstance<ExtensionPoint<*>>() as List<ExtensionPoint<Any>>
            is Collection<*> -> return value.filterIsInstance<ExtensionPoint<*>>() as List<ExtensionPoint<Any>>
        }
        // Fallback: scan the `extensionPoints` field.
        val field = ReflectionAccess.readField(area, "extensionPoints")
        if (field is Map<*, *>) {
            return field.values.filterIsInstance<ExtensionPoint<*>>() as List<ExtensionPoint<Any>>
        }
        throw ReflectionDriftException(
            "Cannot enumerate extension points: neither ExtensionsArea.getExtensionPoints() nor " +
                "the 'extensionPoints' field resolved on ${area.javaClass.name} — internal API drift",
        )
    }

    internal fun extensionPointInfoOf(ep: ExtensionPoint<*>, areaTag: String): ExtensionPointInfo {
        val (kind, beanOrInterface) = kindAndClass(ep)
        val pluginDescriptor = pluginDescriptorOf(ep)
        val dynamic = isDynamic(ep)
        // IMPORTANT: never call ep.extensionList here — it instantiates every extension and
        // surfaces latent registration bugs in other plugins (e.g. com.intellij.java's
        // BuildManager$BuildManagerStartupActivity may not implement ProjectActivity in some
        // builds, which makes the extensionList getter throw and pollute the IDE state).
        // ep.size() returns the adapter count without instantiation.
        val extCount = try { ep.size() } catch (_: Throwable) { 0 }
        return ExtensionPointInfo(
            name = epName(ep),
            kind = kind,
            interfaceOrBeanClass = beanOrInterface,
            declaredByPluginId = pluginDescriptor?.first ?: "unknown",
            declaredByPluginName = pluginDescriptor?.second,
            isDynamic = dynamic,
            extensionsCount = extCount,
            area = areaTag,
        )
    }

    /** [ExtensionPoint] doesn't expose a `name` on the public interface — it's on the impl. */
    internal fun epName(ep: ExtensionPoint<*>): String = try {
        ReflectionAccess.readField(ep, "name")?.toString()
            ?: ReflectionAccess.readMethod(ep, "getName")?.toString()
            ?: ep.javaClass.simpleName
    } catch (_: Throwable) {
        ep.javaClass.simpleName
    }

    internal fun kindAndClass(ep: ExtensionPoint<*>): Pair<String, String> {
        // Read `className` and `getKind()` via reflection only — `ExtensionPointImpl` is
        // marked @ApiStatus.Internal and direct usage trips the plugin verifier. Reflection
        // also handles platform-version drift (`className` / `myClassName`) and falls back
        // to the lazily-resolved Class<*> for EPs registered by Class reference.
        try {
            val kindRaw = ReflectionAccess.readMethod(ep, "getKind")
            val kindStr = when (kindRaw?.toString()) {
                "INTERFACE" -> "INTERFACE"
                "BEAN_CLASS" -> "BEAN_CLASS"
                else -> kindRaw?.toString() ?: "BEAN_CLASS"
            }
            val resolvedName = ReflectionAccess.readField(ep, "className", "myClassName") as? String
                ?: tryReadExtensionClass(ep)
                ?: "?"
            return kindStr to resolvedName
        } catch (_: Throwable) {
            return "BEAN_CLASS" to "?"
        }
    }

    /** Reads the lazily-resolved Class<*> off ExtensionPointImpl. Does NOT instantiate any
     *  extension — only forces classloading of the bean/interface type, which the platform
     *  has already done for any EP that has at least one registered extension. */
    internal fun tryReadExtensionClass(ep: ExtensionPoint<*>): String? =
        (ReflectionAccess.readMethod(ep, "getExtensionClass") as? Class<*>)?.name

    internal fun pluginDescriptorOf(ep: ExtensionPoint<*>): Pair<String, String?>? {
        val pd = ReflectionAccess.readMethod(ep, "getPluginDescriptor", "getDescriptor") ?: return null
        return PluginDescriptorReader.idAndName(pd)
    }

    internal fun isDynamic(ep: ExtensionPoint<*>): Boolean =
        ReflectionAccess.readMethod(ep, "isDynamic") as? Boolean ?: false

    fun listExtensionsForEp(name: String, limit: Int): List<ExtensionInfo> {
        val ep = locateEp(name) ?: return emptyList()
        return extensionsOf(ep, name).take(limit)
    }

    private fun locateEp(epName: String): ExtensionPoint<*>? {
        val app = ApplicationManager.getApplication().extensionArea
        try {
            val maybe = app.getExtensionPointIfRegistered<Any>(epName)
            if (maybe != null) return maybe
        } catch (_: Throwable) {
        }
        for (project in ProjectManager.getInstance().openProjects) {
            try {
                val maybe = project.extensionArea.getExtensionPointIfRegistered<Any>(epName)
                if (maybe != null) return maybe
            } catch (_: Throwable) {
            }
        }
        return null
    }

    /** For each registered extension instance, produce an [ExtensionInfo]. */
    private fun extensionsOf(ep: ExtensionPoint<*>, pointName: String): List<ExtensionInfo> =
        adaptersOf(ep).mapNotNull { adapter ->
            try {
                adapterToExtensionInfo(adapter, pointName)
            } catch (t: Throwable) {
                thisLogger().debug("Failed to read one extension adapter for $pointName", t)
                null
            }
        }

    /** ExtensionPointImpl#sortedAdapters returns the ExtensionComponentAdapter list. */
    private fun adaptersOf(ep: ExtensionPoint<*>): List<Any> {
        val raw = ReflectionAccess.readMethod(ep, "getSortedAdapters", "sortedAdapters")
            ?: throw ReflectionDriftException(
                "Cannot enumerate extensions: neither getSortedAdapters() nor the 'sortedAdapters' " +
                    "member resolved on ${ep.javaClass.name} — internal API drift",
            )
        return (raw as? List<*>)?.filterNotNull()
            ?: throw ReflectionDriftException(
                "getSortedAdapters() on ${ep.javaClass.name} returned ${raw.javaClass.name}, expected List — internal API drift",
            )
    }

    internal fun adapterToExtensionInfo(adapter: Any, pointName: String): ExtensionInfo {
        val implClass = ReflectionAccess.readMethod(adapter, "getAssignableToClassName")?.toString()
            ?: ReflectionAccess.readField(adapter, "implementationClassOrName")?.toString()
            ?: ReflectionAccess.readMethod(adapter, "getOrderId")?.toString()
        // ExtensionComponentAdapter exposes `pluginDescriptor` as a public field, not a getter.
        val pd = ReflectionAccess.readField(adapter, "pluginDescriptor")
            ?: ReflectionAccess.readMethod(adapter, "getPluginDescriptor")
        val pluginId = pd?.let { PluginDescriptorReader.extractPluginIdString(it) } ?: "unknown"
        val pluginName = pd?.let { ReflectionAccess.readMethod(it, "getName")?.toString() }
        val attributes = readAdditionalAttributes(adapter)
        val effectiveClass = ExtensionMetadata.pickEffectiveClass(implClass, attributes)
        return ExtensionInfo(
            extensionPointName = pointName,
            implementationClass = implClass,
            effectiveClass = effectiveClass,
            providedByPluginId = pluginId,
            providedByPluginName = pluginName,
            additionalAttributes = attributes,
        )
    }

    @Suppress("UNCHECKED_CAST")
    internal fun readAdditionalAttributes(adapter: Any): Map<String, String> {
        // Two complementary sources for the XML attributes attached to an extension:
        //   1. `extensionElement: XmlElement` — present until IntelliJ nulls it after instance creation
        //      (which is most of the time for services / tool windows by the time we look).
        //   2. `extensionInstance` — when present, public/JvmField properties of the bean are a
        //      lossless mirror of the original XML attributes.
        // We merge both so newly-loaded EPs and long-lived ones look the same.
        val merged = mutableMapOf<String, String>()
        try {
            val element = ReflectionAccess.readField(adapter, "extensionElement")
            if (element != null) {
                readXmlAttributes(element, merged)
            }
        } catch (_: Throwable) {}
        try {
            val instance = ReflectionAccess.readField(adapter, "extensionInstance")
            if (instance != null) {
                ExtensionMetadata.harvestBeanFields(instance, merged)
            }
        } catch (_: Throwable) {}
        return merged
    }

    private fun readXmlAttributes(element: Any, merged: MutableMap<String, String>) {
        val asMap = ReflectionAccess.readField(element, "attributes") as? Map<*, *>
            ?: ReflectionAccess.readMethod(element, "getAttributes") as? Map<*, *>
        if (asMap != null) {
            for ((key, value) in asMap) {
                if (key != null && value != null) merged[key.toString()] = value.toString()
            }
            return
        }
        val asList = ReflectionAccess.readMethod(element, "getAttributes") as? List<*>
        asList?.forEach { attribute ->
            if (attribute == null) return@forEach
            val name = ReflectionAccess.readMethod(attribute, "getName")?.toString() ?: return@forEach
            val value = ReflectionAccess.readMethod(attribute, "getValue")?.toString() ?: return@forEach
            merged[name] = value
        }
    }
}
