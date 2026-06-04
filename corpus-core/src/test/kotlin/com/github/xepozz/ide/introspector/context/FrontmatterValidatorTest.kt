package com.github.xepozz.ide.introspector.context

import org.junit.Assert.*
import org.junit.Test

class FrontmatterValidatorTest {
    private fun codes(issues: List<ValidationIssue>) = issues.map { it.code }

    private fun hasIssue(issues: List<ValidationIssue>, code: IssueCode, severity: Severity) =
        issues.any { it.code == code && it.severity == severity }

    private fun frontmatter(
        id: String = "my.doc",
        title: String? = "My Document",
        source: Source? = Source.MANUAL,
        kind: Kind? = Kind.SKILL,
        state: State = State.VERIFIED,
        description: String? = "a description",
        verifiedAgainstBuild: String? = null,
        relatedTools: List<String> = emptyList(),
        plugin: String? = null,
        extensionPoint: String? = null,
        idDerivedFromPath: Boolean = false,
    ) = Frontmatter(
        id = id,
        title = title,
        source = source,
        kind = kind,
        state = state,
        description = description,
        verifiedAgainstBuild = verifiedAgainstBuild,
        relatedTools = relatedTools,
        plugin = plugin,
        extensionPoint = extensionPoint,
        idDerivedFromPath = idDerivedFromPath,
    )

    @Test
    fun fully_valid_frontmatter_yields_no_issues() {
        val issues = FrontmatterValidator.validate(frontmatter(), "x.md")

        assertTrue(issues.isEmpty())
    }

    @Test
    fun missing_id_reports_missing_id_error() {
        val issues = FrontmatterValidator.validate(frontmatter(idDerivedFromPath = true), "x.md")

        assertTrue(hasIssue(issues, IssueCode.MISSING_ID, Severity.ERROR))
    }

    @Test
    fun missing_title_reports_missing_title_error() {
        val issues = FrontmatterValidator.validate(frontmatter(title = null), "x.md")

        assertTrue(hasIssue(issues, IssueCode.MISSING_TITLE, Severity.ERROR))
    }

    @Test
    fun missing_source_reports_missing_source_error() {
        val issues = FrontmatterValidator.validate(frontmatter(source = null), "x.md")

        assertTrue(hasIssue(issues, IssueCode.MISSING_SOURCE, Severity.ERROR))
    }

    @Test
    fun missing_kind_reports_missing_kind_error() {
        val issues = FrontmatterValidator.validate(frontmatter(kind = null), "x.md")

        assertTrue(hasIssue(issues, IssueCode.MISSING_KIND, Severity.ERROR))
    }

    @Test
    fun example_kind_without_provenance_reports_error() {
        val issues = FrontmatterValidator.validate(
            frontmatter(kind = Kind.EXAMPLE, plugin = null, extensionPoint = null),
            "x.md",
        )

        assertTrue(hasIssue(issues, IssueCode.EXAMPLE_MISSING_PROVENANCE, Severity.ERROR))
    }

    @Test
    fun example_kind_with_full_provenance_has_no_provenance_error() {
        val issues = FrontmatterValidator.validate(
            frontmatter(kind = Kind.EXAMPLE, plugin = "com.example", extensionPoint = "com.example.ep"),
            "x.md",
        )

        assertFalse(codes(issues).contains(IssueCode.EXAMPLE_MISSING_PROVENANCE))
    }

    @Test
    fun generated_source_without_build_reports_warning() {
        val issues = FrontmatterValidator.validate(
            frontmatter(source = Source.GENERATED, verifiedAgainstBuild = null),
            "x.md",
        )

        assertTrue(hasIssue(issues, IssueCode.GENERATED_MISSING_BUILD, Severity.WARNING))
    }

    @Test
    fun manual_source_without_description_reports_warning() {
        val issues = FrontmatterValidator.validate(
            frontmatter(source = Source.MANUAL, description = null),
            "x.md",
        )

        assertTrue(hasIssue(issues, IssueCode.MANUAL_MISSING_DESCRIPTION, Severity.WARNING))
    }

    @Test
    fun related_tools_without_registry_reports_unverified_warning() {
        val issues = FrontmatterValidator.validate(
            frontmatter(relatedTools = listOf("tool.a")),
            "x.md",
            knownToolNames = null,
        )

        assertTrue(hasIssue(issues, IssueCode.RELATED_TOOLS_UNVERIFIED, Severity.WARNING))
    }

    @Test
    fun related_tool_not_in_registry_reports_unknown_error() {
        val issues = FrontmatterValidator.validate(
            frontmatter(relatedTools = listOf("tool.unknown")),
            "x.md",
            knownToolNames = setOf("tool.known"),
        )

        assertTrue(hasIssue(issues, IssueCode.RELATED_TOOLS_UNKNOWN, Severity.ERROR))
    }

    @Test
    fun related_tool_in_registry_reports_no_issue_for_it() {
        val issues = FrontmatterValidator.validate(
            frontmatter(relatedTools = listOf("tool.known")),
            "x.md",
            knownToolNames = setOf("tool.known"),
        )

        assertFalse(codes(issues).contains(IssueCode.RELATED_TOOLS_UNKNOWN))
        assertFalse(codes(issues).contains(IssueCode.RELATED_TOOLS_UNVERIFIED))
    }

    @Test
    fun state_defaults_to_verified_when_absent() {
        val frontmatter = Frontmatter(
            id = "my.doc",
            title = "My Document",
            source = Source.MANUAL,
            kind = Kind.SKILL,
        )

        assertEquals(State.VERIFIED, frontmatter.state)
    }
}
