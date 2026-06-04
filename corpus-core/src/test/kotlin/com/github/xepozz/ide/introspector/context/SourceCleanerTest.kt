package com.github.xepozz.ide.introspector.context

import org.junit.Assert.*
import org.junit.Test

class SourceCleanerTest {
    private val cleaner = SourceCleaner()

    @Test
    fun strips_leading_line_comment_license_run() {
        val input = "// Copyright 2020\n// All rights reserved\nclass Foo\n"

        assertEquals("class Foo\n", cleaner.clean(input))
    }

    @Test
    fun strips_leading_block_comment_header() {
        val input = "/*\n * License header\n */\nclass Foo\n"

        assertEquals("class Foo\n", cleaner.clean(input))
    }

    @Test
    fun removes_import_runs_without_leaving_sentinel() {
        val input = "package a.b\n\nimport x.Y\nimport z.W\n\nclass Foo\n"
        val cleaned = cleaner.clean(input)

        assertFalse(cleaned.contains("import"))
        assertTrue(cleaned.contains("package a.b"))
        assertTrue(cleaned.contains("class Foo"))
    }

    @Test
    fun removes_line_with_decompiler_field_marker() {
        val input = "class Foo {\n    \$FF: synthetic field\n    val x = 1\n}\n"
        val cleaned = cleaner.clean(input)

        assertFalse(cleaned.contains("\$FF:"))
        assertTrue(cleaned.contains("val x = 1"))
    }

    @Test
    fun removes_synthetic_access_like_Foo_dollar_1() {
        val input = "val a = Foo\$1\nval b = 2\n"
        val cleaned = cleaner.clean(input)

        assertFalse(cleaned.contains("Foo\$1"))
        assertTrue(cleaned.contains("val b = 2"))
    }

    @Test
    fun keeps_nested_class_reference_Foo_dollar_Bar() {
        val input = "val a = Foo\$Bar\nval b = 2\n"
        val cleaned = cleaner.clean(input)

        assertTrue(cleaned.contains("Foo\$Bar"))
    }

    @Test
    fun collapses_three_or_more_blank_lines_to_one() {
        val input = "a\n\n\n\nb\n"
        val cleaned = cleaner.clean(input)

        assertEquals("a\n\nb\n", cleaned)
    }

    @Test
    fun ensures_single_trailing_newline() {
        val cleaned = cleaner.clean("a\nb")

        assertTrue(cleaned.endsWith("\n"))
        assertFalse(cleaned.endsWith("\n\n"))
    }

    @Test
    fun is_idempotent() {
        val input = "// header\n\n\nimport x.Y\n\n\n\nclass Foo\$1\nval keep = 1\n\n\n"
        val once = cleaner.clean(input)
        val twice = cleaner.clean(once)

        assertEquals(once, twice)
    }

    @Test
    fun empty_input_yields_empty_output() {
        assertEquals("", cleaner.clean(""))
    }
}
