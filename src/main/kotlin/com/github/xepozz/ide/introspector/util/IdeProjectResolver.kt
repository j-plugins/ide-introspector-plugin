package com.github.xepozz.ide.introspector.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.IdeFocusManager

object IdeProjectResolver {
    fun focusedProject(): Project? {
        val openProjects = ProjectManager.getInstance().openProjects.filterNot { it.isDisposed }
        if (openProjects.size <= 1) return openProjects.firstOrNull()
        val lastFocused = runCatching { IdeFocusManager.getGlobalInstance().lastFocusedFrame?.project }.getOrNull()
        return lastFocused?.takeIf { it in openProjects } ?: openProjects.first()
    }
}
