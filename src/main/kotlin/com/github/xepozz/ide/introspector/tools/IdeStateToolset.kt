package com.github.xepozz.ide.introspector.tools

import com.github.xepozz.ide.introspector.model.IndexingStatusResponse
import com.github.xepozz.ide.introspector.util.IdeProjectResolver
import com.intellij.mcpserver.McpExpectedError
import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import kotlinx.serialization.json.JsonObject

class IdeStateToolset : McpToolset {

    @McpTool(name = "ide.indexing_status")
    @McpDescription(
        """
        |Reports whether the IDE is in dumb mode (indexing) or smart mode (ready) for the focused
        |project.
        |
        |Use this when:
        |  - Before running psi.* / find-usages / navigation tools, or before relying on resolved
        |    code — to confirm the index is built.
        |  - You want to poll until indexing finishes before trusting symbol resolution.
        |
        |Do NOT use this when:
        |  - You just want the open files — psi.list_open_files is the right tool. This reports only
        |    the index state, not editor contents.
        |
        |Returns: { projectName, indexing, ready }. `indexing=true` means dumb mode (results from
        |index-dependent tools may be incomplete); `ready=true` means smart mode (index is built).
        |
        |Example:
        |  (no args)   — report the indexing state of the focused project
        """
    )
    suspend fun ide_indexing_status(): IndexingStatusResponse {
        val project = requireProject()
        val dumb = DumbService.getInstance(project).isDumb
        return IndexingStatusResponse(
            projectName = project.name,
            indexing = dumb,
            ready = !dumb,
        )
    }

    private fun requireProject(): Project = IdeProjectResolver.focusedProject()
        ?: throw McpExpectedError(
            "No open project. Open a project in this IDE first.",
            JsonObject(emptyMap())
        )
}
