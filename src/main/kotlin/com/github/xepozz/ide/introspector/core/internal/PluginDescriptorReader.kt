package com.github.xepozz.ide.introspector.core.internal

import com.github.xepozz.ide.introspector.util.ReflectionAccess

object PluginDescriptorReader {

    fun extractPluginIdString(pluginDescriptor: Any): String? {
        val pluginId = ReflectionAccess.readMethod(pluginDescriptor, "getPluginId")
            ?: ReflectionAccess.readField(pluginDescriptor, "pluginId")
            ?: return null
        return ReflectionAccess.readMethod(pluginId, "getIdString")?.toString()
            ?: ReflectionAccess.readField(pluginId, "idString")?.toString()
            ?: pluginId.toString()
    }

    fun idAndName(pluginDescriptor: Any): Pair<String, String?> {
        val id = extractPluginIdString(pluginDescriptor) ?: "unknown"
        val name = ReflectionAccess.readMethod(pluginDescriptor, "getName")?.toString()
        return id to name
    }
}
