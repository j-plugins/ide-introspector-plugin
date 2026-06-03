package com.github.xepozz.ide.introspector.model

import kotlinx.serialization.Serializable

/**
 * One resolution target for a single PsiReference. References can resolve to multiple targets
 * (PsiPolyVariantReference, e.g. an overloaded method call, an ambiguous identifier) — see
 * [ResolvedReference.targets] for the list.
 *
 * `sameFile` is set when the target lives in the same FileViewProvider as the source — typical
 * for local variables / private methods. When sameFile=false, `targetFileUrl` points at the
 * declaring file (could be a library jar, another project module, etc.).
 */
@Serializable
data class ResolveTarget(
    val resolved: Boolean,
    val targetPsiClass: String? = null,
    val targetText: String? = null,
    val targetRange: TextRangeInfo? = null,
    val targetFileUrl: String? = null,
    val sameFile: Boolean = false,
    /** Name of the target if it implements PsiNamedElement — e.g. variable name, method name. */
    val declarationName: String? = null,
)

@Serializable
data class ResolvedReference(
    /** Stable id of the source PsiElement, matching [PsiNode.id] from a sibling psi.get_structure call. */
    val sourceNodeId: String? = null,
    val sourcePsiClass: String,
    val sourceText: String,
    /** Absolute range in the host file of the *reference* (element textRange + ref rangeInElement). */
    val sourceRange: TextRangeInfo,
    val referenceClass: String,
    /** Soft references (e.g. completion-only) may not light up Ctrl-click but still resolve. */
    val isSoft: Boolean = false,
    val targets: List<ResolveTarget> = emptyList(),
)

@Serializable
data class GetReferencesResponse(
    val fileUrl: String,
    val scope: String,
    val references: List<ResolvedReference>,
    val total: Int,
    val truncated: Boolean = false,
    val warnings: List<String> = emptyList(),
)
