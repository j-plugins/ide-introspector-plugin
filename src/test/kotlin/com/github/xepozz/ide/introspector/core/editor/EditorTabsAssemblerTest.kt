package com.github.xepozz.ide.introspector.core.editor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorTabsAssemblerTest {

    private fun rawTab(
        windowIndex: Int,
        path: String,
        selected: Boolean = false,
        pinned: Boolean = false,
        modified: Boolean = false,
    ) = RawEditorTab(
        windowIndex = windowIndex,
        path = path,
        name = path.substringAfterLast('/'),
        fileType = "XML",
        selected = selected,
        pinned = pinned,
        modified = modified,
    )

    @Test
    fun `groups tabs by window index`() {
        val response = EditorTabsAssembler.assemble(
            projectName = "demo",
            rawTabs = listOf(
                rawTab(0, "/a/one.kt"),
                rawTab(1, "/a/two.kt"),
                rawTab(0, "/a/three.kt"),
            ),
            activeFile = null,
        )
        assertEquals(2, response.windows.size)
        assertEquals(listOf("one.kt", "three.kt"), response.windows[0].tabs.map { it.name })
        assertEquals(listOf("two.kt"), response.windows[1].tabs.map { it.name })
    }

    @Test
    fun `windows are sorted by window index`() {
        val response = EditorTabsAssembler.assemble(
            projectName = "demo",
            rawTabs = listOf(
                rawTab(2, "/a/c.kt"),
                rawTab(0, "/a/a.kt"),
                rawTab(1, "/a/b.kt"),
            ),
            activeFile = null,
        )
        assertEquals(listOf(0, 1, 2), response.windows.map { it.windowIndex })
    }

    @Test
    fun `total counts every tab across windows`() {
        val response = EditorTabsAssembler.assemble(
            projectName = "demo",
            rawTabs = listOf(
                rawTab(0, "/a/a.kt"),
                rawTab(0, "/a/b.kt"),
                rawTab(1, "/a/c.kt"),
            ),
            activeFile = null,
        )
        assertEquals(3, response.total)
    }

    @Test
    fun `maps tab attributes and active file through`() {
        val response = EditorTabsAssembler.assemble(
            projectName = "demo",
            rawTabs = listOf(rawTab(0, "/a/main.kt", selected = true, pinned = true, modified = true)),
            activeFile = "/a/main.kt",
        )
        val tab = response.windows.single().tabs.single()
        assertEquals("/a/main.kt", tab.path)
        assertEquals("main.kt", tab.name)
        assertEquals("XML", tab.fileType)
        assertTrue(tab.selected)
        assertTrue(tab.pinned)
        assertTrue(tab.modified)
        assertEquals("/a/main.kt", response.activeFile)
        assertEquals("demo", response.projectName)
    }

    @Test
    fun `empty input yields no windows and zero total`() {
        val response = EditorTabsAssembler.assemble("demo", emptyList(), activeFile = null)
        assertTrue(response.windows.isEmpty())
        assertEquals(0, response.total)
        assertEquals(null, response.activeFile)
    }
}
