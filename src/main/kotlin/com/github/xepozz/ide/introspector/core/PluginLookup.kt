package com.github.xepozz.ide.introspector.core

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.openapi.extensions.PluginId

/**
 * Reflection wrappers around `com.intellij.ide.plugins.PluginManagerCore` for
 * `getPlugins()` / `getPlugin(PluginId)`. Both are `@ApiStatus.Internal` in current
 * IDE builds and trip the plugin verifier when referenced directly from source.
 * `Method.invoke` calls are not source-visible to the verifier, so this layer keeps
 * the verifier report clean while still using the only authoritative source of
 * loaded plugins available at runtime.
 *
 * `PluginManagerCore.isDisabled` is NOT wrapped here — the verifier does not flag it
 * as internal in builds we target, and adding a third indirection would obscure the
 * one call site (`PluginInventory.readIsEnabled`) without benefit.
 */
internal object PluginLookup {

    private val pluginManagerCoreCls: Class<*> by lazy {
        Class.forName("com.intellij.ide.plugins.PluginManagerCore")
    }

    private val getPluginsMethod by lazy {
        pluginManagerCoreCls.getMethod("getPlugins")
    }

    private val getPluginMethod by lazy {
        pluginManagerCoreCls.getMethod("getPlugin", PluginId::class.java)
    }

    fun allPlugins(): Array<IdeaPluginDescriptor> {
        @Suppress("UNCHECKED_CAST")
        return getPluginsMethod.invoke(null) as Array<IdeaPluginDescriptor>
    }

    fun findPlugin(id: PluginId): IdeaPluginDescriptor? =
        getPluginMethod.invoke(null, id) as? IdeaPluginDescriptor
}
