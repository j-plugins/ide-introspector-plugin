package com.github.xepozz.ide.introspector.core

import com.github.xepozz.ide.introspector.core.internal.ContainerDescriptorReader
import com.github.xepozz.ide.introspector.model.ListenerInfo
import com.github.xepozz.ide.introspector.util.ReflectionAccess
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.openapi.diagnostic.thisLogger

/**
 * Reads `<applicationListeners>` and `<projectListeners>` declarations off the live
 * [com.intellij.ide.plugins.IdeaPluginDescriptorImpl.appContainerDescriptor] /
 * `projectContainerDescriptor`. Uses reflection because both [com.intellij.util.messages.ListenerDescriptor]
 * and the container class are `@ApiStatus.Internal`.
 *
 * Runtime `messageBus.connect().subscribe(...)` subscribers are out of scope — they're attached
 * imperatively, not declaratively, and the platform doesn't expose a deterministic enumeration.
 */
object ListenerInspector {

    fun listAll(): List<ListenerInfo> {
        val out = mutableListOf<ListenerInfo>()
        for (descriptor in PluginLookup.allPlugins()) {
            collectFor(descriptor, out)
        }
        return out
    }

    fun listForPlugin(descriptor: IdeaPluginDescriptor): List<ListenerInfo> {
        val out = mutableListOf<ListenerInfo>()
        collectFor(descriptor, out)
        return out
    }

    private fun collectFor(descriptor: IdeaPluginDescriptor, out: MutableList<ListenerInfo>) {
        val pluginId = descriptor.pluginId.idString
        val pluginName = descriptor.name
        out += ContainerDescriptorReader.collectFromContainers(
            descriptor = descriptor,
            areaGetters = AREA_GETTERS,
            fieldName = "listeners",
        ) { element, areaTag ->
            try {
                toListenerInfo(element, areaTag, pluginId, pluginName)
            } catch (t: Throwable) {
                thisLogger().debug("Failed to read ListenerDescriptor for $pluginId/$areaTag", t)
                null
            }
        }
    }

    internal fun toListenerInfo(
        ld: Any,
        areaTag: String,
        pluginId: String,
        pluginName: String?,
    ): ListenerInfo? {
        val listenerClass = ReflectionAccess.readField(ld, "listenerClassName")?.toString() ?: return null
        val topicClass = ReflectionAccess.readField(ld, "topicClassName")?.toString() ?: return null
        val activeInTest = (ReflectionAccess.readField(ld, "activeInTestMode") as? Boolean) ?: true
        val activeInHeadless = (ReflectionAccess.readField(ld, "activeInHeadlessMode") as? Boolean) ?: true
        val os = ReflectionAccess.readEnumName(ld, "os")
        return ListenerInfo(
            topicClass = topicClass,
            listenerClass = listenerClass,
            area = areaTag,
            activeInTestMode = activeInTest,
            activeInHeadlessMode = activeInHeadless,
            os = os,
            providedByPluginId = pluginId,
            providedByPluginName = pluginName,
        )
    }

    private val AREA_GETTERS = listOf(
        "application" to "getAppContainerDescriptor",
        "project" to "getProjectContainerDescriptor",
    )
}
