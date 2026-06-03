package com.github.xepozz.ide.introspector.model

import kotlinx.serialization.Serializable

/**
 * The declaration `psi.find_usages` resolved to. Either picked up from a reference under the
 * caret (Ctrl-click semantics) or — when the caret sits on a declaration site — the nearest
 * containing [com.intellij.psi.PsiNamedElement]. The agent can use this to confirm that the
 * platform agreed with its mental model before reading the usage list.
 */
@Serializable
data class TargetInfo(
    val psiClass: String,
    val declarationName: String? = null,
    val fileUrl: String,
    val range: TextRangeInfo,
    /** First-line preview of the declaration, truncated. */
    val text: String,
)

/**
 * A single usage site found by `psi.find_usages`.
 *
 * `kind` distinguishes:
 *   "reference"      — classic call/read/write site discovered via ReferencesSearch
 *   "implementation" — an overriding method or implementing/extending class discovered via
 *                      DefinitionsScopedSearch (same source as Ctrl+Alt+B "Goto Implementation")
 *
 * `lineSnippet` is the trimmed-and-truncated line of code the usage sits on — what IntelliJ's
 * Find Usages tool window shows beside each hit. Far more useful to an agent than a bare
 * text range, since it provides surrounding context without a follow-up file read.
 *
 * `containingDeclaration` is the enclosing method/class/field name (the "in foo() of class Bar"
 * grouping in the Find Usages tool window). Lets the agent answer "where is this used?" in
 * human-meaningful terms.
 */
@Serializable
data class UsageInfo(
    val kind: String,
    val fileUrl: String,
    val fileType: String,
    val range: TextRangeInfo,
    val text: String,
    val lineSnippet: String,
    val referenceClass: String? = null,
    val isSoft: Boolean = false,
    val containingDeclaration: String? = null,
)

/** Group of usages in one file — populated only when groupByFile=true. */
@Serializable
data class FileUsages(
    val fileUrl: String,
    val fileType: String,
    val usages: List<UsageInfo>,
)

@Serializable
data class FindUsagesResponse(
    val target: TargetInfo,
    val scope: String,
    val usages: List<UsageInfo> = emptyList(),
    val byFile: List<FileUsages> = emptyList(),
    val total: Int,
    val truncated: Boolean = false,
    val warnings: List<String> = emptyList(),
)
