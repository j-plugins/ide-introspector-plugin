package com.github.xepozz.ide.introspector.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ClassNamesTest {

    @Test
    fun `strips the package from a fully qualified name`() {
        assertEquals("Tree", "com.intellij.ui.treeStructure.Tree".simpleClassName())
    }

    @Test
    fun `strips the enclosing class from an inner class name`() {
        assertEquals(
            "MyProjectViewTree",
            "com.intellij.ide.projectView.impl.ProjectViewPane\$MyProjectViewTree".simpleClassName(),
        )
    }

    @Test
    fun `leaves a bare simple name untouched`() {
        assertEquals("JButton", "JButton".simpleClassName())
    }
}
