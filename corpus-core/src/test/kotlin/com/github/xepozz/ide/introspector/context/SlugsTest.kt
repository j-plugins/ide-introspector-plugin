package com.github.xepozz.ide.introspector.context

import org.junit.Assert.*
import org.junit.Test

class SlugsTest {
    @Test
    fun spaces_become_single_dashes_and_lowercased() {
        assertEquals("threading-model", Slugs.slugify("Threading Model"))
        assertEquals("kotlin-coroutines", Slugs.slugify("Kotlin Coroutines"))
    }

    @Test
    fun parens_and_spaces_collapse_to_single_dashes_without_edge_dashes() {
        assertEquals(
            "program-structure-interface-psi",
            Slugs.slugify("Program Structure Interface (PSI)"),
        )
    }

    @Test
    fun runs_of_non_alphanumerics_collapse_to_one_dash() {
        assertEquals("a-b", Slugs.slugify("a   ---  b"))
        assertEquals("a-b", Slugs.slugify("a&&&b"))
    }

    @Test
    fun leading_and_trailing_non_alphanumerics_are_trimmed() {
        assertEquals("hello", Slugs.slugify("  (hello)!  "))
        assertEquals("hello-world", Slugs.slugify("---hello world---"))
    }

    @Test
    fun digits_are_preserved() {
        assertEquals("ide-2024-edition", Slugs.slugify("IDE 2024 Edition"))
    }
}
