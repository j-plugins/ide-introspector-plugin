package com.github.xepozz.ide.introspector.model

import kotlinx.serialization.Serializable

/** Absolute text range in the host (top-level) file, plus 1-based line/column for human readability. */
@Serializable
data class TextRangeInfo(
    val startOffset: Int,
    val endOffset: Int,
    val startLine: Int,
    val startColumn: Int,
    val endLine: Int,
    val endColumn: Int,
)

/**
 * One PSI element node in the flat pre-order DFS of [PsiFileTree.nodes]. The `id` / `parentId`
 * pair encodes the tree without nesting:
 *   - root nodes have parentId == null and id "<rootIdx>"
 *   - child of "0" at position 3 has id "0.3", etc.
 *
 * `hasReferences` is the cheap signal — `element.references.isNotEmpty()` doesn't resolve, so it's
 * safe to compute on every node, and lets the agent skip the heavy `psi.get_references` call on
 * leaves that obviously can't have one (whitespace, comments).
 */
@Serializable
data class PsiNode(
    val id: String,
    val parentId: String? = null,
    val psiClass: String,
    /** ASTNode.elementType.toString() — token type for leaves ("IDENTIFIER", "WHITE_SPACE"), grammar rule for composites. */
    val elementType: String,
    val textRange: TextRangeInfo,
    val text: String? = null,
    val hasReferences: Boolean = false,
    /** True when this element can host injected languages (PsiLanguageInjectionHost). */
    val isInjectionHost: Boolean = false,
    val childCount: Int = 0,
)

/**
 * One language root inside a file's FileViewProvider. A `.php` file produces two of these
 * (PHP + HTML); a `.vue` file typically produces four (Vue + JavaScript + CSS + HTML). The
 * `psiFileClass` lets the agent distinguish them programmatically.
 */
@Serializable
data class PsiFileTree(
    val language: String,
    val psiFileClass: String,
    val nodes: List<PsiNode>,
    /** Set when the per-tree node limit was hit — the tree is a prefix of the real one. */
    val truncated: Boolean = false,
)

/**
 * Injected language anchored on a host PsiElement inside one of the [GetPsiStructureResponse.psiFiles].
 *
 * Examples: SQL injected into a Kotlin string literal, regex injected into a JS string, JavaScript
 * in an HTML `<script>` tag. The injection's offsets in `nodes[].textRange` are relative to the
 * INJECTED document; `hostRange` maps the injection back onto the host file's offsets so the
 * agent can correlate the two.
 */
@Serializable
data class PsiInjectionTree(
    val hostNodeId: String?,                // matches PsiNode.id of the host element, if found
    val hostRange: TextRangeInfo,           // where the injection sits in the host file
    val tree: PsiFileTree,
)

@Serializable
data class GetPsiStructureResponse(
    val fileUrl: String,
    val fileType: String,
    val length: Int,
    val psiFiles: List<PsiFileTree>,
    val injections: List<PsiInjectionTree> = emptyList(),
    val truncated: Boolean = false,
    val nodeCount: Int = 0,
    val warnings: List<String> = emptyList(),
)
