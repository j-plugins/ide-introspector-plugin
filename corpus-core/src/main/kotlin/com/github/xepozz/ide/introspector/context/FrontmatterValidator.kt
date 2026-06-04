package com.github.xepozz.ide.introspector.context

object FrontmatterValidator {
    fun validate(
        frontmatter: Frontmatter,
        sourcePath: String,
        knownToolNames: Set<String>? = null,
    ): List<ValidationIssue> = buildList {
        validateId(frontmatter, sourcePath)
        validateTitle(frontmatter, sourcePath)
        validateSource(frontmatter, sourcePath)
        validateKind(frontmatter, sourcePath)
        validateExampleProvenance(frontmatter, sourcePath)
        validateSourceConstraints(frontmatter, sourcePath)
        validateRelatedTools(frontmatter, sourcePath, knownToolNames)
    }

    private fun MutableList<ValidationIssue>.validateId(frontmatter: Frontmatter, sourcePath: String) {
        if (frontmatter.idDerivedFromPath) {
            add(
                ValidationIssue(
                    severity = Severity.ERROR,
                    code = IssueCode.MISSING_ID,
                    message = "Required key 'id' is missing; it was derived from the file path.",
                    key = "id",
                    sourcePath = sourcePath,
                ),
            )
            return
        }
        if (frontmatter.id.isBlank()) {
            add(
                ValidationIssue(
                    severity = Severity.ERROR,
                    code = IssueCode.BLANK_REQUIRED_VALUE,
                    message = "Required key 'id' must not be blank.",
                    key = "id",
                    sourcePath = sourcePath,
                ),
            )
        }
    }

    private fun MutableList<ValidationIssue>.validateTitle(frontmatter: Frontmatter, sourcePath: String) {
        val title = frontmatter.title
        if (title == null) {
            add(
                ValidationIssue(
                    severity = Severity.ERROR,
                    code = IssueCode.MISSING_TITLE,
                    message = "Required key 'title' is missing.",
                    key = "title",
                    sourcePath = sourcePath,
                ),
            )
            return
        }
        if (title.isBlank()) {
            add(
                ValidationIssue(
                    severity = Severity.ERROR,
                    code = IssueCode.BLANK_REQUIRED_VALUE,
                    message = "Required key 'title' must not be blank.",
                    key = "title",
                    sourcePath = sourcePath,
                ),
            )
        }
    }

    private fun MutableList<ValidationIssue>.validateSource(frontmatter: Frontmatter, sourcePath: String) {
        if (frontmatter.source == null) {
            add(
                ValidationIssue(
                    severity = Severity.ERROR,
                    code = IssueCode.MISSING_SOURCE,
                    message = "Required key 'source' is missing.",
                    key = "source",
                    sourcePath = sourcePath,
                ),
            )
        }
    }

    private fun MutableList<ValidationIssue>.validateKind(frontmatter: Frontmatter, sourcePath: String) {
        if (frontmatter.kind == null) {
            add(
                ValidationIssue(
                    severity = Severity.ERROR,
                    code = IssueCode.MISSING_KIND,
                    message = "Required key 'kind' is missing.",
                    key = "kind",
                    sourcePath = sourcePath,
                ),
            )
        }
    }

    private fun MutableList<ValidationIssue>.validateExampleProvenance(
        frontmatter: Frontmatter,
        sourcePath: String,
    ) {
        if (frontmatter.kind != Kind.EXAMPLE) {
            return
        }
        if (frontmatter.plugin.isNullOrBlank() || frontmatter.extensionPoint.isNullOrBlank()) {
            add(
                ValidationIssue(
                    severity = Severity.ERROR,
                    code = IssueCode.EXAMPLE_MISSING_PROVENANCE,
                    message = "Example documents require both 'plugin' and 'ep' provenance keys.",
                    sourcePath = sourcePath,
                ),
            )
        }
    }

    private fun MutableList<ValidationIssue>.validateSourceConstraints(
        frontmatter: Frontmatter,
        sourcePath: String,
    ) {
        if (frontmatter.source == Source.GENERATED && frontmatter.verifiedAgainstBuild == null) {
            add(
                ValidationIssue(
                    severity = Severity.WARNING,
                    code = IssueCode.GENERATED_MISSING_BUILD,
                    message = "Generated documents should declare 'verifiedAgainstBuild'.",
                    key = "verifiedAgainstBuild",
                    sourcePath = sourcePath,
                ),
            )
        }
        if (frontmatter.source == Source.MANUAL && frontmatter.description == null) {
            add(
                ValidationIssue(
                    severity = Severity.WARNING,
                    code = IssueCode.MANUAL_MISSING_DESCRIPTION,
                    message = "Manual documents should declare a 'description'.",
                    key = "description",
                    sourcePath = sourcePath,
                ),
            )
        }
    }

    private fun MutableList<ValidationIssue>.validateRelatedTools(
        frontmatter: Frontmatter,
        sourcePath: String,
        knownToolNames: Set<String>?,
    ) {
        val relatedTools = frontmatter.relatedTools
        if (relatedTools.isEmpty()) {
            return
        }
        if (knownToolNames == null) {
            add(
                ValidationIssue(
                    severity = Severity.WARNING,
                    code = IssueCode.RELATED_TOOLS_UNVERIFIED,
                    message = "Related tools could not be verified; no known tool registry was supplied.",
                    key = "related_tools",
                    sourcePath = sourcePath,
                ),
            )
            return
        }
        for (toolName in relatedTools) {
            if (toolName !in knownToolNames) {
                add(
                    ValidationIssue(
                        severity = Severity.ERROR,
                        code = IssueCode.RELATED_TOOLS_UNKNOWN,
                        message = "Related tool '$toolName' is not a known tool name.",
                        key = "related_tools",
                        sourcePath = sourcePath,
                    ),
                )
            }
        }
    }
}
