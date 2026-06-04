package com.github.xepozz.ide.introspector.context

import org.junit.Assert.*
import org.junit.Test

class FrontmatterParserTest {
    private fun codes(issues: List<ValidationIssue>) = issues.map { it.code }

    private fun hasIssue(issues: List<ValidationIssue>, code: IssueCode, severity: Severity) =
        issues.any { it.code == code && it.severity == severity }

    private val minimalHeader = """
        |---
        |id: my.doc
        |title: My Document
        |source: manual
        |kind: skill
        |---
        |Body text here.
    """.trimMargin()

    @Test
    fun parses_minimal_valid_header_into_non_null_frontmatter() {
        val parsed = FrontmatterParser.parse(minimalHeader, "docs/my.md")

        val frontmatter = parsed.frontmatter
        assertNotNull(frontmatter)
        assertEquals("my.doc", frontmatter!!.id)
        assertEquals("My Document", frontmatter.title)
        assertEquals(Source.MANUAL, frontmatter.source)
        assertEquals(Kind.SKILL, frontmatter.kind)
    }

    @Test
    fun body_is_text_after_closing_fence_with_one_leading_newline_trimmed() {
        val raw = "---\nid: x\ntitle: T\n---\nfirst line\nsecond line"

        val parsed = FrontmatterParser.parse(raw, "x.md")

        assertEquals("first line\nsecond line", parsed.body)
    }

    @Test
    fun strips_byte_order_mark_before_fence() {
        val raw = "﻿---\nid: x\ntitle: T\n---\nbody"

        val parsed = FrontmatterParser.parse(raw, "x.md")

        assertNotNull(parsed.frontmatter)
        assertEquals("body", parsed.body)
    }

    @Test
    fun normalizes_crlf_line_endings() {
        val raw = "---\r\nid: x\r\ntitle: T\r\n---\r\nbody line"

        val parsed = FrontmatterParser.parse(raw, "x.md")

        assertNotNull(parsed.frontmatter)
        assertEquals("x", parsed.frontmatter!!.id)
        assertEquals("body line", parsed.body)
    }

    @Test
    fun normalizes_lone_carriage_return_line_endings() {
        val raw = "---\rid: x\rtitle: T\r---\rbody line"

        val parsed = FrontmatterParser.parse(raw, "x.md")

        assertNotNull(parsed.frontmatter)
        assertEquals("x", parsed.frontmatter!!.id)
        assertEquals("body line", parsed.body)
    }

    @Test
    fun value_containing_colon_is_preserved() {
        val raw = "---\nid: x\ntitle: T\ndescription: foo: bar\n---\nbody"

        val parsed = FrontmatterParser.parse(raw, "x.md")

        assertEquals("foo: bar", parsed.frontmatter!!.description)
    }

    @Test
    fun parses_flat_list() {
        val raw = "---\nid: x\ntitle: T\ntags: [a, b, c]\n---\nbody"

        val parsed = FrontmatterParser.parse(raw, "x.md")

        assertEquals(listOf("a", "b", "c"), parsed.frontmatter!!.tags)
    }

    @Test
    fun empty_list_yields_empty_list() {
        val raw = "---\nid: x\ntitle: T\ntags: []\n---\nbody"

        val parsed = FrontmatterParser.parse(raw, "x.md")

        assertTrue(parsed.frontmatter!!.tags.isEmpty())
    }

    @Test
    fun trailing_comma_in_list_drops_empty_element() {
        val raw = "---\nid: x\ntitle: T\ntags: [a, b,]\n---\nbody"

        val parsed = FrontmatterParser.parse(raw, "x.md")

        assertEquals(listOf("a", "b"), parsed.frontmatter!!.tags)
    }

    @Test
    fun unclosed_list_produces_malformed_list_error() {
        val raw = "---\nid: x\ntitle: T\ntags: [a, b\n---\nbody"

        val parsed = FrontmatterParser.parse(raw, "x.md")

        assertTrue(hasIssue(parsed.parseIssues, IssueCode.MALFORMED_LIST, Severity.ERROR))
    }

    @Test
    fun duplicate_key_keeps_last_value_and_warns() {
        val raw = "---\nid: x\ntitle: First\ntitle: Second\n---\nbody"

        val parsed = FrontmatterParser.parse(raw, "x.md")

        assertEquals("Second", parsed.frontmatter!!.title)
        assertTrue(hasIssue(parsed.parseIssues, IssueCode.DUPLICATE_KEY, Severity.WARNING))
    }

    @Test
    fun unknown_key_is_collected_and_warned() {
        val raw = "---\nid: x\ntitle: T\nbogus: value\n---\nbody"

        val parsed = FrontmatterParser.parse(raw, "x.md")

        assertTrue(parsed.frontmatter!!.unknownKeys.contains("bogus"))
        assertTrue(hasIssue(parsed.parseIssues, IssueCode.UNKNOWN_KEY, Severity.WARNING))
    }

    @Test
    fun no_opening_fence_returns_null_frontmatter_and_whole_body() {
        val raw = "no fence here\njust text"

        val parsed = FrontmatterParser.parse(raw, "x.md")

        assertNull(parsed.frontmatter)
        assertEquals(raw, parsed.body)
        assertTrue(hasIssue(parsed.parseIssues, IssueCode.MISSING_FRONTMATTER, Severity.WARNING))
    }

    @Test
    fun opening_fence_with_no_closing_returns_unterminated_error() {
        val raw = "---\nid: x\ntitle: T\nbody without closing fence"

        val parsed = FrontmatterParser.parse(raw, "x.md")

        assertNull(parsed.frontmatter)
        assertTrue(hasIssue(parsed.parseIssues, IssueCode.UNTERMINATED_FRONTMATTER, Severity.ERROR))
    }

    @Test
    fun line_without_colon_is_malformed_line_error() {
        val raw = "---\nid: x\nthis line has no colon\ntitle: T\n---\nbody"

        val parsed = FrontmatterParser.parse(raw, "x.md")

        assertTrue(hasIssue(parsed.parseIssues, IssueCode.MALFORMED_LINE, Severity.ERROR))
    }

    @Test
    fun enum_mapping_is_case_insensitive() {
        val raw = "---\nid: x\ntitle: T\nsource: Generated\nkind: SKILL\n---\nbody"

        val parsed = FrontmatterParser.parse(raw, "x.md")

        assertEquals(Source.GENERATED, parsed.frontmatter!!.source)
        assertEquals(Kind.SKILL, parsed.frontmatter.kind)
    }

    @Test
    fun invalid_enum_value_leaves_field_null_and_errors() {
        val raw = "---\nid: x\ntitle: T\nkind: nonsense\n---\nbody"

        val parsed = FrontmatterParser.parse(raw, "x.md")

        assertNull(parsed.frontmatter!!.kind)
        assertTrue(hasIssue(parsed.parseIssues, IssueCode.INVALID_ENUM, Severity.ERROR))
    }

    @Test
    fun id_is_derived_from_path_when_absent() {
        val raw = "---\ntitle: T\n---\nbody"

        val parsed = FrontmatterParser.parse(raw, "docs/My File.md")

        val frontmatter = parsed.frontmatter!!
        assertTrue(frontmatter.idDerivedFromPath)
        assertEquals("docs.my-file", frontmatter.id)
    }

    @Test
    fun wire_keys_map_to_frontmatter_fields() {
        val raw = """
            |---
            |id: x
            |title: T
            |when_to_use: while editing
            |related_eps: [ep.one, ep.two]
            |related_tools: [tool.a]
            |ep: com.example.ep
            |---
            |body
        """.trimMargin()

        val parsed = FrontmatterParser.parse(raw, "x.md")

        val frontmatter = parsed.frontmatter!!
        assertEquals("while editing", frontmatter.whenToUse)
        assertEquals(listOf("ep.one", "ep.two"), frontmatter.relatedExtensionPoints)
        assertEquals(listOf("tool.a"), frontmatter.relatedTools)
        assertEquals("com.example.ep", frontmatter.extensionPoint)
    }

    @Test
    fun empty_body_is_allowed() {
        val raw = "---\nid: x\ntitle: T\n---\n"

        val parsed = FrontmatterParser.parse(raw, "x.md")

        assertNotNull(parsed.frontmatter)
        assertEquals("", parsed.body)
    }
}
