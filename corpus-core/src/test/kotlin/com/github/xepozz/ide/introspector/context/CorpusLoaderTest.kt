package com.github.xepozz.ide.introspector.context

import org.junit.Assert.*
import org.junit.Test

class CorpusLoaderTest {
    private fun codes(issues: List<ValidationIssue>) = issues.map { it.code }

    private fun hasIssue(issues: List<ValidationIssue>, code: IssueCode, severity: Severity) =
        issues.any { it.code == code && it.severity == severity }

    private fun loaderOf(vararg files: RawFile): CorpusLoader =
        CorpusLoader(FileSource { files.toList() })

    private fun manualText(id: String): String =
        """
            |---
            |id: $id
            |title: Manual Doc
            |source: manual
            |kind: skill
            |description: A manual document.
            |---
            |Body of $id.
        """.trimMargin()

    @Test
    fun valid_manual_file_becomes_entry_with_manual_layer_and_source() {
        val result = loaderOf(RawFile("manual/skills/foo.md", manualText("manual.foo"))).load()

        assertEquals(1, result.entries.size)
        val entry = result.entries.single()
        assertEquals("manual.foo", entry.id)
        assertEquals(CorpusLayer.MANUAL, entry.layer)
        assertEquals(Source.MANUAL, entry.source)
    }

    @Test
    fun files_are_processed_in_relative_path_order() {
        val result = loaderOf(
            RawFile("manual/c.md", manualText("c")),
            RawFile("manual/a.md", manualText("a")),
            RawFile("manual/b.md", manualText("b")),
        ).load()

        assertEquals(
            listOf("manual/a.md", "manual/b.md", "manual/c.md"),
            result.entries.map { it.relativePath },
        )
    }

    @Test
    fun file_without_frontmatter_is_dropped_and_yields_missing_frontmatter_error() {
        val result = loaderOf(RawFile("manual/no-front.md", "just some body text")).load()

        assertTrue(result.entries.isEmpty())
        assertTrue(hasIssue(result.issues, IssueCode.MISSING_FRONTMATTER, Severity.ERROR))
    }

    @Test
    fun parser_and_validator_issues_are_folded_into_result_issues() {
        val text =
            """
                |---
                |title: No Id Doc
                |source: manual
                |kind: skill
                |bogus_key: value
                |---
                |Body.
            """.trimMargin()
        val result = loaderOf(RawFile("manual/x.md", text)).load()

        assertTrue(hasIssue(result.issues, IssueCode.UNKNOWN_KEY, Severity.WARNING))
        assertTrue(hasIssue(result.issues, IssueCode.MISSING_ID, Severity.ERROR))
    }

    @Test
    fun example_with_non_matching_path_yields_warning_but_keeps_entry() {
        val text =
            """
                |---
                |id: ex.bad.path
                |title: Example
                |source: generated
                |kind: example
                |plugin: com.example
                |ep: com.example.ep
                |verifiedAgainstBuild: 251.1
                |---
                |Example body.
            """.trimMargin()
        val result = loaderOf(RawFile("generated/examples/loose.md", text)).load()

        assertEquals(1, result.entries.size)
        assertEquals(CorpusLayer.EXAMPLES, result.entries.single().layer)
        assertTrue(hasIssue(result.issues, IssueCode.EXAMPLE_PATH_MISMATCH, Severity.WARNING))
    }

    @Test
    fun example_with_matching_path_yields_no_path_mismatch_warning() {
        val text =
            """
                |---
                |id: ex.good.path
                |title: Example
                |source: generated
                |kind: example
                |plugin: com.example
                |ep: com.example.ep
                |verifiedAgainstBuild: 251.1
                |---
                |Example body.
            """.trimMargin()
        val result = loaderOf(
            RawFile("generated/examples/plugins/com.example/com.example.ep.md", text),
        ).load()

        assertEquals(1, result.entries.size)
        assertFalse(codes(result.issues).contains(IssueCode.EXAMPLE_PATH_MISMATCH))
    }

    @Test
    fun declared_source_disagreeing_with_layer_yields_layer_source_mismatch_warning() {
        val text =
            """
                |---
                |id: mismatch.doc
                |title: Mismatch
                |source: generated
                |kind: concept
                |verifiedAgainstBuild: 251.1
                |---
                |Body.
            """.trimMargin()
        val result = loaderOf(RawFile("manual/mismatch.md", text)).load()

        assertEquals(1, result.entries.size)
        assertTrue(hasIssue(result.issues, IssueCode.LAYER_SOURCE_MISMATCH, Severity.WARNING))
    }

    @Test
    fun example_kotlin_fence_is_cleaned_while_markdown_preserved() {
        val text =
            """
                |---
                |id: ex.fence
                |title: Example
                |source: generated
                |kind: example
                |plugin: com.example
                |ep: com.example.ep
                |verifiedAgainstBuild: 251.1
                |---
                |Intro paragraph.
                |
                |```kotlin
                |import a.b.C
                |class Demo
                |```
                |
                |Outro paragraph.
            """.trimMargin()
        val result = loaderOf(
            RawFile("generated/examples/plugins/com.example/com.example.ep.md", text),
        ).load()

        val body = result.entries.single().body
        assertFalse(body.contains("import a.b.C"))
        assertTrue(body.contains("class Demo"))
        assertTrue(body.contains("Intro paragraph."))
        assertTrue(body.contains("Outro paragraph."))
        assertTrue(body.contains("```kotlin"))
    }
}
