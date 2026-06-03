package com.github.xepozz.ide.introspector.model

import kotlinx.serialization.Serializable

/**
 * One open editor tab (or "active" tab) reported by [com.github.xepozz.ide.introspector.tools.PsiToolset.psi_list_open_files].
 *
 * The pair (path, url) lets the agent either log a human-readable path or feed `url` back into
 * the rest of the `psi.*` tools — the URL form survives in-memory / scratch buffers / jar:// roots
 * that don't have a real filesystem path.
 *
 * `viewProviderLanguages` reports the multi-PSI nature of the file as IntelliJ sees it. A `.php`
 * file in IntelliJ has two language roots in its FileViewProvider (`PHP` + `HTML`) and any
 * `psi.get_structure` call will return both; this is the cheap hint that you should expect more
 * than one root, without parsing the file.
 */
@Serializable
data class OpenFileInfo(
    val path: String,
    val url: String,
    val fileType: String,
    val viewProviderLanguages: List<String>,
    val length: Int,
    /** Only set for the focused tab — caret offset in the editor's document. */
    val caretOffset: Int? = null,
)

@Serializable
data class OpenFilesResponse(
    val projectName: String,
    /** The currently focused tab in the project, or null if no editor is selected. */
    val activeFile: OpenFileInfo? = null,
    val openFiles: List<OpenFileInfo> = emptyList(),
)
