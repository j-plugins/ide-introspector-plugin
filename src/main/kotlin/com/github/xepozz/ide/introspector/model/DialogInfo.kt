package com.github.xepozz.ide.introspector.model

import kotlinx.serialization.Serializable

/**
 * Semantic descriptor of one open dialog window. See [DialogsResponse].
 *
 * `id` is a [com.github.xepozz.ide.introspector.core.ComponentRegistry] handle that the
 * caller can reuse with `ui.get_properties`, `ui.get_tree` (via `rootSelector="dialog"`),
 * or `screenshot.capture(target='component')`. The id is stable across calls within one
 * IDE session.
 */
@Serializable
data class DialogInfo(
    val id: String,
    val title: String? = null,
    val isModal: Boolean,
    val isResizable: Boolean,
    val isShowing: Boolean,
    val bounds: Bounds,
    val contentClass: String,
)

@Serializable
data class DialogsResponse(
    val dialogs: List<DialogInfo> = emptyList(),
    val warnings: List<String> = emptyList(),
)
