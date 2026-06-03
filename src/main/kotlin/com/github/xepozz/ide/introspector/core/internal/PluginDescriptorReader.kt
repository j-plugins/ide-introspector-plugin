package com.github.xepozz.ide.introspector.core.internal

import com.intellij.openapi.extensions.PluginDescriptor

object PluginDescriptorReader {

    fun extractPluginIdString(pluginDescriptor: Any): String? =
        (pluginDescriptor as? PluginDescriptor)?.pluginId?.idString

    fun idAndName(pluginDescriptor: Any): Pair<String, String?> {
        val descriptor = pluginDescriptor as? PluginDescriptor ?: return "unknown" to null
        return (descriptor.pluginId?.idString ?: "unknown") to descriptor.name
    }
}
