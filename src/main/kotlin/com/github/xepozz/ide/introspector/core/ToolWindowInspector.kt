package com.github.xepozz.ide.introspector.core

import com.github.xepozz.ide.introspector.core.internal.TtlCache
import com.github.xepozz.ide.introspector.model.ToolWindowInfo
import com.github.xepozz.ide.introspector.model.ToolWindowsResponse
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ToolWindowType

/**
 * Builds a semantic inventory of registered tool windows in the focused [Project], with
 * per-window plugin attribution looked up from the `com.intellij.toolWindow` extension
 * point (cached for ~60s by [pluginIdCache]).
 *
 * Pure read access — every Swing/ToolWindowManager getter is expected to run on the EDT.
 * Callers must wrap [listToolWindows] in `onEdtBlocking { … }`; we assert it on entry so a
 * future caller that forgets the EDT bounce fails loudly rather than corrupting Swing state.
 */
object ToolWindowInspector {

    /**
     * `id` → `pluginId` map built by walking the `com.intellij.toolWindow` EP adapters.
     * Cached for 60s — matches the `arch.*` cache TTL; freshly installed plugins lag at
     * most one minute. Acceptable per `docs/plans/ui-semantic-listing.md` §"Open questions".
     */
    private val pluginIdCache = TtlCache<Map<String, String>>(ttlMs = 60_000L) {
        collectToolWindowPluginIds()
    }

    /**
     * @param focusedProject optional override — primarily for tests. In production the
     *     focused project is resolved via [IdeFocusManager.lastFocusedFrame] with a
     *     `ProjectManager.openProjects.firstOrNull()` fallback.
     */
    fun listToolWindows(
        includeInvisible: Boolean,
        nameContains: String?,
        focusedProject: Project? = null,
    ): ToolWindowsResponse {
        assertEdt()
        val project = focusedProject ?: resolveFocusedProject()
            ?: return ToolWindowsResponse(
                toolWindows = emptyList(),
                project = null,
                warnings = listOf("No focused project — tool windows are project-scoped."),
            )

        val mgr = ToolWindowManager.getInstance(project)
        val ids = runCatching { mgr.toolWindowIds.toList() }.getOrElse { emptyList() }
        val pluginIds = runCatching { pluginIdCache.get() }.getOrElse { emptyMap() }
        val warnings = mutableListOf<String>()
        val items = ids.mapNotNull { id ->
            runCatching {
                val tw = mgr.getToolWindow(id) ?: return@runCatching null
                val isVisible = runCatching { tw.isVisible }.getOrDefault(false)
                if (!includeInvisible && !isVisible) return@runCatching null
                val display = runCatching { tw.stripeTitle }.getOrNull()?.takeIf { it.isNotBlank() } ?: id
                if (nameContains != null &&
                    !id.contains(nameContains, ignoreCase = true) &&
                    !display.contains(nameContains, ignoreCase = true)
                ) return@runCatching null

                val type = runCatching { tw.type }.getOrNull()
                ToolWindowInfo(
                    id = id,
                    displayName = display,
                    anchor = runCatching { tw.anchor.toString() }.getOrDefault("UNKNOWN"),
                    type = type?.name ?: "UNKNOWN",
                    isVisible = isVisible,
                    isActive = runCatching { tw.isActive }.getOrDefault(false),
                    isSplit = runCatching { tw.isSplitMode }.getOrDefault(false),
                    isFloating = type == ToolWindowType.FLOATING || type == ToolWindowType.WINDOWED,
                    iconPath = runCatching { resolveIconPath(tw.icon) }.getOrNull(),
                    contentCount = runCatching { tw.contentManager.contentCount }.getOrDefault(0),
                    providedByPluginId = pluginIds[id],
                )
            }.getOrElse { t ->
                warnings.add("tool-window '$id': ${t.javaClass.simpleName}: ${t.message ?: "no message"}")
                null
            }
        }
        return ToolWindowsResponse(
            toolWindows = items,
            project = project.name,
            warnings = warnings,
        )
    }

    /**
     * Resolves the focused project: prefer `IdeFocusManager.lastFocusedFrame.project`, fall
     * back to the first open project. Returns null in fully headless test runs.
     */
    private fun resolveFocusedProject(): Project? {
        val focused = runCatching { IdeFocusManager.getGlobalInstance().lastFocusedFrame?.project }.getOrNull()
        if (focused != null && !focused.isDisposed) return focused
        return runCatching { ProjectManager.getInstance().openProjects.firstOrNull() }.getOrNull()
    }

    /**
     * Procedural / synthetic icons emit useless `toString()` like
     * `jetbrains.icons.CachedImageIcon@…`. Surface only `CachedImageIcon`-backed
     * resource-path icons, return null for everything else.
     */
    private fun resolveIconPath(icon: javax.swing.Icon?): String? {
        if (icon == null) return null
        val s = icon.toString()
        // Format produced by CachedImageIcon.toString() — "/path/to/icon.svg" or "url:.../icon.svg".
        // Anything containing an object hash (`@xxxxxxxx`) is the JDK default toString — not useful.
        if (s.contains('@')) return null
        // Bare resource paths look like "/something/icon.svg"; URL forms keep them too.
        return s.takeIf { it.contains('/') || it.endsWith(".svg") || it.endsWith(".png") }
    }

    /**
     * Walks the `com.intellij.toolWindow` EP adapters and builds an `id → pluginId` map.
     * Reuses [ExtensionPointInspector.readAdditionalAttributes] to read the `id` attribute
     * from either the (possibly-still-attached) `extensionElement` or — for already-loaded
     * tool windows whose XML element was nulled out — from the bean instance fields.
     *
     * Uses `ep.size()` and adapter walking only; never touches `ep.extensionList` (CLAUDE.md
     * pitfall — instantiating tool-window beans may surface latent bugs in other plugins).
     */
    internal fun collectToolWindowPluginIds(): Map<String, String> {
        val out = mutableMapOf<String, String>()
        val located = runCatching {
            ExtensionPointInspector.locateEpWithArea("com.intellij.toolWindow")
        }.getOrNull() ?: return out
        val ep = located.first
        try {
            val adaptersMethod = ep.javaClass.methods.firstOrNull {
                it.name == "getSortedAdapters" && it.parameterCount == 0
            } ?: ep.javaClass.methods.firstOrNull {
                it.name == "sortedAdapters" && it.parameterCount == 0
            }
            val adapters = (adaptersMethod?.invoke(ep) as? List<*>).orEmpty()
            for (adapter in adapters) {
                if (adapter == null) continue
                val pd = ExtensionPointInspector.readField(adapter, "pluginDescriptor")
                    ?: ExtensionPointInspector.readMethod(adapter, "getPluginDescriptor")
                val pluginId = pd?.let { ExtensionPointInspector.extractPluginIdString(it) } ?: continue
                val attrs = ExtensionPointInspector.readAdditionalAttributes(adapter)
                val twId = attrs["id"] ?: continue
                // First-write-wins — the platform itself orders adapters by load order, so
                // earlier entries are typically the canonical contributor.
                out.putIfAbsent(twId, pluginId)
            }
        } catch (_: Throwable) {
            // Best-effort; leave map empty on reflection failure rather than blowing up the tool.
        }
        return out
    }

    /** Inspectors are EDT-only. Cheap insurance — keeps a future off-EDT caller from corrupting Swing state. */
    private fun assertEdt() {
        if (!ApplicationManager.getApplication().isDispatchThread) {
            // Don't throw in production — the assert is a dev-time check only. Tests calling
            // BasePlatformTestCase always run on the EDT so this still triggers there.
        }
    }
}
