package com.github.xepozz.ide.introspector.core

import com.github.xepozz.ide.introspector.core.internal.TtlCache
import com.github.xepozz.ide.introspector.model.ListenerInfo
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Enumerates MessageBus listeners declared in `plugin.xml` (`<applicationListeners>` /
 * `<projectListeners>`) across every loaded plugin.
 *
 * The underlying types — `IdeaPluginDescriptorImpl`, `ContainerDescriptor`,
 * `ListenerDescriptor` — are `@ApiStatus.Internal` in the platform and have churned
 * across IntelliJ versions, so we reach into them reflectively (reusing
 * [ExtensionPointInspector.readField]) rather than via a `compileOnly` import.
 *
 * Listeners registered programmatically through `MessageBus.connect().subscribe(...)`
 * are NOT visible here — those live on the per-component `MessageBusImpl`, see the
 * `arch.list_listeners` tool description for the rationale on keeping that out of
 * scope for v1.
 */
@Service(Service.Level.APP)
class ListenerInspector {

    private val cache = TtlCache<List<ListenerInfo>>(ttlMs = CACHE_TTL_MS) { collect() }

    /** Full list of declared listeners (cached for ~60s; same for every caller). */
    fun list(forceRefresh: Boolean = false): List<ListenerInfo> = cache.get(forceRefresh)

    /** Invalidate the cache; the next [list] call rebuilds. */
    fun refresh() { cache.invalidate() }

    private fun collect(): List<ListenerInfo> {
        val out = mutableListOf<ListenerInfo>()
        @Suppress("UnstableApiUsage")
        val descriptors = PluginManagerCore.plugins
        for (descriptor in descriptors) {
            try {
                val pluginId = descriptor.pluginId.idString
                val pluginName = descriptor.name
                collectFromDescriptor(descriptor, pluginId, pluginName, out)
            } catch (t: Throwable) {
                thisLogger().debug("Failed to collect listeners for ${descriptor.pluginId.idString}", t)
            }
        }
        return out
    }

    /**
     * Reads the `app` / `project` [ContainerDescriptor]s off [descriptor] and appends
     * one [ListenerInfo] per [ListenerDescriptor]. Visible for tests — the parameter is
     * `Any` precisely so a stub object can stand in for `IdeaPluginDescriptorImpl`.
     */
    internal fun collectFromDescriptor(
        descriptor: Any,
        pluginId: String,
        pluginName: String?,
        out: MutableList<ListenerInfo>,
    ) {
        // The field names on IdeaPluginDescriptorImpl are `appContainerDescriptor` /
        // `projectContainerDescriptor` in 252. Earlier 251.x builds used `app` / `project` —
        // try the canonical names first, fall through to the legacy aliases for safety.
        collectScope(descriptor, scope = "application",
            containerFields = listOf("appContainerDescriptor", "app"),
            pluginId, pluginName, out)
        collectScope(descriptor, scope = "project",
            containerFields = listOf("projectContainerDescriptor", "project"),
            pluginId, pluginName, out)
    }

    private fun collectScope(
        descriptor: Any,
        scope: String,
        containerFields: List<String>,
        pluginId: String,
        pluginName: String?,
        out: MutableList<ListenerInfo>,
    ) {
        val container = containerFields.asSequence()
            .mapNotNull { ExtensionPointInspector.readField(descriptor, it) }
            .firstOrNull() ?: run {
                warnContainerMissOnce(scope, descriptor, containerFields)
                return
            }
        val listeners = ExtensionPointInspector.readField(container, "listeners") as? List<*> ?: return
        for (ld in listeners) {
            if (ld == null) continue
            val info = toListenerInfoOrNull(ld, scope, pluginId, pluginName) ?: continue
            out += info
        }
    }

    /**
     * One-shot `WARN` per scope (one for `application`, one for `project`) when neither
     * canonical nor legacy container-descriptor field resolves on the FIRST plugin we hit
     * in that state — guards against a silent empty list if JetBrains renames the field
     * a third time in a future build. Subsequent misses are swallowed so a stub descriptor
     * (Android Studio fork, custom test descriptor, …) can't spam the log.
     *
     * The warning names the descriptor class, the fields tried, and the IDE build so a
     * user can file an actionable bug. `ApplicationInfo` is looked up reflectively because
     * unit tests drive `collectScope` without the IntelliJ runtime.
     */
    private fun warnContainerMissOnce(scope: String, descriptor: Any, fields: List<String>) {
        val flag = if (scope == "application") appContainerMissLogged else projectContainerMissLogged
        if (!flag.compareAndSet(false, true)) return
        val build = runCatching {
            val klass = Class.forName(
                "com.intellij.openapi.application.ApplicationInfo",
                false,
                ListenerInspector::class.java.classLoader,
            )
            val info = klass.getMethod("getInstance").invoke(null)
            klass.getMethod("getBuild").invoke(info)?.toString()
        }.getOrNull() ?: "unknown"
        thisLogger().warn(
            "[ListenerInspector] $scope-scope container descriptor not found on " +
                "${descriptor.javaClass.name}; tried fields ${fields.joinToString(", ")}. " +
                "Likely a JetBrains field rename — arch.list_listeners will return an empty " +
                "list for the $scope scope on this build ($build)."
        )
    }

    /** One-shot latches — see [warnContainerMissOnce]. */
    private val appContainerMissLogged = AtomicBoolean(false)
    private val projectContainerMissLogged = AtomicBoolean(false)

    /**
     * Converts a single platform `ListenerDescriptor` (or any shape with matching field
     * names) into a [ListenerInfo]. Returns `null` when the entry is too malformed to
     * surface (missing topic / listener class name).
     *
     * Handles the field-rename fallback documented in the plan: a few 251.x builds
     * renamed `topicClassName` → `topicClass` and `listenerClassName` → `listenerClass`.
     * If the canonical name is absent, we try the alternate once before giving up.
     */
    internal fun toListenerInfoOrNull(
        ld: Any,
        scope: String,
        pluginId: String,
        pluginName: String?,
    ): ListenerInfo? {
        val topic = ExtensionPointInspector.readField(ld, "topicClassName") as? String
            ?: ExtensionPointInspector.readField(ld, "topicClass") as? String
            ?: return null
        val listener = ExtensionPointInspector.readField(ld, "listenerClassName") as? String
            ?: ExtensionPointInspector.readField(ld, "listenerClass") as? String
            ?: return null
        val testMode = (ExtensionPointInspector.readField(ld, "activeInTestMode") as? Boolean) ?: true
        val headless = (ExtensionPointInspector.readField(ld, "activeInHeadlessMode") as? Boolean) ?: true
        val os = ExtensionPointInspector.readField(ld, "os")?.toString()
        return ListenerInfo(
            topicClass = topic,
            listenerClass = listener,
            scope = scope,
            providedByPluginId = pluginId,
            providedByPluginName = pluginName,
            activeInTestMode = testMode,
            activeInHeadlessMode = headless,
            os = os,
        )
    }

    companion object {
        const val CACHE_TTL_MS = 60_000L
        fun getInstance(): ListenerInspector = service()
    }
}
