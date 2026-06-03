package com.github.xepozz.ide.introspector.tools

import com.github.xepozz.ide.introspector.core.editor.EditorTabsAssembler
import com.github.xepozz.ide.introspector.core.editor.RawEditorTab
import com.github.xepozz.ide.introspector.model.ActiveEditorResponse
import com.github.xepozz.ide.introspector.model.EditorTabsResponse
import com.github.xepozz.ide.introspector.util.onEdtBlocking
import com.github.xepozz.ide.introspector.util.requireFocusedProject
import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class EditorToolset : McpToolset {

    @McpTool(name = "editor.list_tabs")
    @McpDescription(
        """
        |Lists the open editor tabs of the focused project, grouped by split window, and marks
        |the selected tab in each window plus the globally active file. Reads the live
        |FileEditorManager — no PSI parsing.
        |
        |Use this when: you need to know which files are open as editor tabs, how they are
        |split across editor windows, which is pinned/modified, and which one is focused.
        |
        |Do NOT use this when: you want the PSI/language view of open files (use
        |psi.list_open_files), or the caret/selection of the active editor (use
        |editor.get_active), or tabs of a tool window (those are not editor tabs).
        |
        |Returns: { projectName, windows: [{ windowIndex, tabs: [{ path, name, fileType,
        |selected, pinned, modified }] }], activeFile, total }. `selected` marks the chosen tab
        |within its split window; `activeFile` is the path of the currently focused editor.
        |
        |Example:
        |  (no args)   — list every open editor tab across all split windows
        """
    )
    suspend fun `editor_list_tabs`(): EditorTabsResponse {
        val project = requireFocusedProject("(editor.* tools operate on an open editor)")
        return onEdtBlocking { collectTabs(project) }
    }

    @McpTool(name = "editor.get_active")
    @McpDescription(
        """
        |Returns the currently focused text editor: its file, caret position, and selection.
        |Caret line and column are 1-based (as the editor shows them); offsets are 0-based
        |character positions in the document.
        |
        |Use this when: you need to know where the user is editing — the active file, the caret
        |location, or the selected text/range — before a navigation or edit.
        |
        |Do NOT use this when: you want all open tabs (use editor.list_tabs), or the focused
        |editor is not a text editor (then `file` is reported but caret fields are -1 with a
        |warning).
        |
        |Returns: { projectName, file, fileType, caretLine, caretColumn, caretOffset,
        |selectionText, selectionStartOffset, selectionEndOffset, modified, warnings }.
        |Caret fields are -1 and selection is null when there is no active text editor.
        |
        |Example:
        |  (no args)   — report the active editor's file, caret, and selection
        """
    )
    suspend fun `editor_get_active`(): ActiveEditorResponse {
        val project = requireFocusedProject("(editor.* tools operate on an open editor)")
        return onEdtBlocking { collectActive(project) }
    }

    private fun collectTabs(project: Project): EditorTabsResponse {
        val manager = FileEditorManagerEx.getInstanceEx(project)
        val documentManager = FileDocumentManager.getInstance()
        val selectedFiles = manager.selectedFiles.toSet()
        val windows = runCatching { manager.windows.toList() }.getOrDefault(emptyList())
        val rawTabs = mutableListOf<RawEditorTab>()
        if (windows.isNotEmpty()) {
            windows.forEachIndexed { windowIndex, window ->
                val files = runCatching { window.fileList }.getOrDefault(emptyList())
                for (file in files) {
                    val pinned = runCatching { window.isFilePinned(file) }.getOrDefault(false)
                    rawTabs.add(rawTab(windowIndex, file, file in selectedFiles, pinned, documentManager))
                }
            }
        } else {
            for (file in manager.openFiles) {
                rawTabs.add(rawTab(0, file, file in selectedFiles, false, documentManager))
            }
        }
        val activeFile = (manager.currentFile ?: selectedFiles.firstOrNull())?.path
        return EditorTabsAssembler.assemble(project.name, rawTabs, activeFile)
    }

    private fun rawTab(
        windowIndex: Int,
        file: VirtualFile,
        selected: Boolean,
        pinned: Boolean,
        documentManager: FileDocumentManager,
    ): RawEditorTab =
        RawEditorTab(
            windowIndex = windowIndex,
            path = file.path,
            name = file.name,
            fileType = runCatching { file.fileType.name }.getOrNull(),
            selected = selected,
            pinned = pinned,
            modified = runCatching { documentManager.isFileModified(file) }.getOrDefault(false),
        )

    private fun collectActive(project: Project): ActiveEditorResponse {
        val manager = FileEditorManagerEx.getInstanceEx(project)
        val file = manager.currentFile
        val editor = manager.selectedTextEditor
            ?: return ActiveEditorResponse(
                projectName = project.name,
                file = file?.path,
                fileType = file?.fileType?.name,
                warnings = listOf("No active text editor (the focused tab may be a non-text editor)."),
            )
        val caret = editor.caretModel.currentCaret
        val position = caret.logicalPosition
        val selectionModel = editor.selectionModel
        val hasSelection = selectionModel.hasSelection()
        return ActiveEditorResponse(
            projectName = project.name,
            file = file?.path,
            fileType = file?.fileType?.name,
            caretLine = position.line + 1,
            caretColumn = position.column + 1,
            caretOffset = caret.offset,
            selectionText = selectionModel.selectedText,
            selectionStartOffset = if (hasSelection) selectionModel.selectionStart else -1,
            selectionEndOffset = if (hasSelection) selectionModel.selectionEnd else -1,
            modified = file?.let { runCatching { FileDocumentManager.getInstance().isFileModified(it) }.getOrDefault(false) } ?: false,
        )
    }
}
