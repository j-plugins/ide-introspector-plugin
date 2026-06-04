package com.github.xepozz.ide.introspector.context

enum class IssueCode {
    MISSING_ID,
    MISSING_TITLE,
    MISSING_SOURCE,
    MISSING_KIND,
    INVALID_ENUM,
    MALFORMED_LINE,
    MALFORMED_LIST,
    UNTERMINATED_FRONTMATTER,
    EXAMPLE_MISSING_PROVENANCE,
    BLANK_REQUIRED_VALUE,
    DUPLICATE_ID_IN_TIER,
    LAYER_SOURCE_MISMATCH,
    UNKNOWN_LAYER,
    MANUAL_OVERRIDE_NO_TARGET,
    EXAMPLE_PATH_MISMATCH,
    MISSING_FRONTMATTER,
    UNKNOWN_KEY,
    DUPLICATE_KEY,
    GENERATED_MISSING_BUILD,
    MANUAL_MISSING_DESCRIPTION,
    RELATED_TOOLS_UNVERIFIED,
    RELATED_TOOLS_UNKNOWN,
    DEAD_INTERNAL_LINK,
    SIZE_BUDGET_EXCEEDED,
}

data class ValidationIssue(
    val severity: Severity,
    val code: IssueCode,
    val message: String,
    val key: String? = null,
    val sourcePath: String? = null,
)
