package com.github.xepozz.ide.introspector.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the pure helpers in `PsiText.kt`.
 *
 * Only [firstNonBlankLine] and the [LOCAL_VARIABLE_LIKE] constant are exercised here — both are
 * IDE-free. [psiClassName] needs a real [com.intellij.psi.PsiElement] and is covered by the
 * platform suite, so it is not faked here.
 */
class PsiTextTest {

    @Test
    fun `firstNonBlankLine returns the only line of single-line text`() {
        assertEquals("fun foo()", firstNonBlankLine("fun foo()"))
    }

    @Test
    fun `firstNonBlankLine trims leading and trailing whitespace`() {
        assertEquals("class Bar", firstNonBlankLine("   class Bar   "))
    }

    @Test
    fun `firstNonBlankLine skips leading blank lines`() {
        assertEquals("first", firstNonBlankLine("\n\n   \n  first\nsecond"))
    }

    @Test
    fun `firstNonBlankLine treats whitespace-only lines as blank`() {
        assertEquals("real", firstNonBlankLine("\t \n  \n\treal\t"))
    }

    @Test
    fun `firstNonBlankLine returns empty string when text is empty`() {
        assertEquals("", firstNonBlankLine(""))
    }

    @Test
    fun `firstNonBlankLine returns empty string when every line is blank`() {
        assertEquals("", firstNonBlankLine("   \n\t\n  \n"))
    }

    @Test
    fun `firstNonBlankLine handles carriage-return-newline line endings`() {
        assertEquals("alpha", firstNonBlankLine("\r\nalpha\r\nbeta"))
    }

    @Test
    fun `LOCAL_VARIABLE_LIKE lists the known local and parameter PSI class names`() {
        assertEquals(
            setOf(
                "PsiLocalVariable",
                "PsiParameter",
                "KtParameter",
                "KtDestructuringDeclarationEntry",
                "JSParameter",
            ),
            LOCAL_VARIABLE_LIKE,
        )
    }

    @Test
    fun `LOCAL_VARIABLE_LIKE does not contain declaration class names`() {
        assertTrue("PsiMethod" !in LOCAL_VARIABLE_LIKE)
        assertTrue("PsiClass" !in LOCAL_VARIABLE_LIKE)
        assertTrue("PsiField" !in LOCAL_VARIABLE_LIKE)
    }
}
