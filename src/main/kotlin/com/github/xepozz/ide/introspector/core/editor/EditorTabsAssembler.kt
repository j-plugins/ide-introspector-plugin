package com.github.xepozz.ide.introspector.core.editor

import com.github.xepozz.ide.introspector.model.EditorTabInfo
import com.github.xepozz.ide.introspector.model.EditorTabsResponse
import com.github.xepozz.ide.introspector.model.EditorWindowInfo

data class RawEditorTab(
    val windowIndex: Int,
    val path: String,
    val name: String,
    val fileType: String?,
    val selected: Boolean,
    val pinned: Boolean,
    val modified: Boolean,
)

object EditorTabsAssembler {
    fun assemble(projectName: String, rawTabs: List<RawEditorTab>, activeFile: String?): EditorTabsResponse {
        val windows = rawTabs
            .groupBy { it.windowIndex }
            .toSortedMap()
            .map { (windowIndex, tabsInWindow) ->
                EditorWindowInfo(
                    windowIndex = windowIndex,
                    tabs = tabsInWindow.map { tab ->
                        EditorTabInfo(
                            path = tab.path,
                            name = tab.name,
                            fileType = tab.fileType,
                            selected = tab.selected,
                            pinned = tab.pinned,
                            modified = tab.modified,
                        )
                    },
                )
            }
        return EditorTabsResponse(
            projectName = projectName,
            windows = windows,
            activeFile = activeFile,
            total = rawTabs.size,
        )
    }
}
