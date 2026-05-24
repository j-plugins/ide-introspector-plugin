package com.github.xepozz.ide.introspector.model

import kotlinx.serialization.Serializable

/**
 * Semantic descriptor of one registered tool window. See [ToolWindowsResponse].
 *
 * Field shape mirrors the contract documented in `docs/plans/ui-semantic-listing.md`.
 * Boolean fields use the `is` prefix to match the underlying `ToolWindow` getters.
 */
@Serializable
data class ToolWindowInfo(
    val id: String,
    val displayName: String,
    val anchor: String,                       // LEFT | RIGHT | BOTTOM | TOP
    val type: String,                         // DOCKED | FLOATING | SLIDING | WINDOWED
    val isVisible: Boolean,
    val isActive: Boolean,
    val isSplit: Boolean,
    val isFloating: Boolean,
    val iconPath: String? = null,
    val contentCount: Int,
    val providedByPluginId: String? = null,
)

@Serializable
data class ToolWindowsResponse(
    val toolWindows: List<ToolWindowInfo> = emptyList(),
    val project: String? = null,
    val warnings: List<String> = emptyList(),
)
