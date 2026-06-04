package com.github.xepozz.ide.introspector.context

import org.junit.Assert.*
import org.junit.Test

class CorpusMergerTest {
    private fun codes(issues: List<ValidationIssue>) = issues.map { it.code }

    private fun hasIssue(issues: List<ValidationIssue>, code: IssueCode, severity: Severity) =
        issues.any { it.code == code && it.severity == severity }

    private fun frontmatterOf(id: String, source: Source): Frontmatter =
        Frontmatter(
            id = id,
            title = "Title $id",
            source = source,
            kind = Kind.CONCEPT,
        )

    private fun entryOf(
        id: String,
        source: Source,
        relativePath: String,
        layer: CorpusLayer = if (source == Source.MANUAL) CorpusLayer.MANUAL else CorpusLayer.SDK_PLATFORM,
    ): CorpusEntry =
        CorpusEntry(
            id = id,
            frontmatter = frontmatterOf(id, source),
            body = "body of $relativePath",
            relativePath = relativePath,
            layer = layer,
            source = source,
        )

    @Test
    fun manual_wins_over_generated_with_same_id_without_error() {
        val manual = entryOf("dup", Source.MANUAL, "manual/dup.md")
        val generated = entryOf("dup", Source.GENERATED, "generated/sdk-platform/dup.md")

        val result = CorpusMerger.merge(listOf(generated, manual))

        assertEquals(1, result.entries.size)
        val winner = result.entries.single()
        assertEquals(Source.MANUAL, winner.source)
        assertEquals("manual/dup.md", winner.relativePath)
        assertTrue(result.issues.isEmpty())
    }

    @Test
    fun two_generated_with_same_id_yield_duplicate_error_and_smallest_path_wins() {
        val a = entryOf("dup", Source.GENERATED, "generated/sdk-platform/b.md")
        val b = entryOf("dup", Source.GENERATED, "generated/sdk-platform/a.md")

        val result = CorpusMerger.merge(listOf(a, b))

        assertEquals(1, result.entries.size)
        assertEquals("generated/sdk-platform/a.md", result.entries.single().relativePath)
        assertTrue(hasIssue(result.issues, IssueCode.DUPLICATE_ID_IN_TIER, Severity.ERROR))
    }

    @Test
    fun standalone_manual_entry_is_kept_without_warning() {
        val manual = entryOf("solo", Source.MANUAL, "manual/solo.md")

        val result = CorpusMerger.merge(listOf(manual))

        assertEquals(1, result.entries.size)
        assertEquals("solo", result.entries.single().id)
        assertTrue(result.issues.isEmpty())
    }

    @Test
    fun output_is_sorted_by_id_ascending() {
        val result = CorpusMerger.merge(
            listOf(
                entryOf("charlie", Source.MANUAL, "manual/charlie.md"),
                entryOf("alpha", Source.MANUAL, "manual/alpha.md"),
                entryOf("bravo", Source.MANUAL, "manual/bravo.md"),
            ),
        )

        assertEquals(listOf("alpha", "bravo", "charlie"), result.entries.map { it.id })
    }

    @Test
    fun empty_input_yields_empty_result_with_no_issues() {
        val result = CorpusMerger.merge(emptyList())

        assertTrue(result.entries.isEmpty())
        assertTrue(result.issues.isEmpty())
    }
}
