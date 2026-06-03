package com.github.xepozz.ide.introspector.model

import kotlinx.serialization.Serializable

@Serializable
data class EditorTabInfo(
    val path: String,
    val name: String,
    val fileType: String? = null,
    val selected: Boolean = false,
    val pinned: Boolean = false,
    val modified: Boolean = false,
)

@Serializable
data class EditorWindowInfo(
    val windowIndex: Int,
    val tabs: List<EditorTabInfo>,
)

@Serializable
data class EditorTabsResponse(
    val projectName: String,
    val windows: List<EditorWindowInfo>,
    val activeFile: String? = null,
    val total: Int = 0,
)

@Serializable
data class ActiveEditorResponse(
    val projectName: String,
    val file: String? = null,
    val fileType: String? = null,
    val caretLine: Int = -1,
    val caretColumn: Int = -1,
    val caretOffset: Int = -1,
    val selectionText: String? = null,
    val selectionStartOffset: Int = -1,
    val selectionEndOffset: Int = -1,
    val modified: Boolean = false,
    val warnings: List<String> = emptyList(),
)
