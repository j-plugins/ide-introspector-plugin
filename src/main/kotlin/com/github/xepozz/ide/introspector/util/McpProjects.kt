package com.github.xepozz.ide.introspector.util

import com.intellij.mcpserver.McpExpectedError
import com.intellij.openapi.project.Project
import kotlinx.serialization.json.JsonObject

fun mcpError(message: String): Nothing =
    throw McpExpectedError(message, JsonObject(emptyMap()))

fun requireFocusedProject(detail: String? = null): Project =
    IdeProjectResolver.focusedProject()
        ?: mcpError(
            listOfNotNull("No open project. Open a project in this IDE first.", detail).joinToString(" "),
        )
