package com.github.xepozz.ide.introspector.core

import com.intellij.psi.PsiElement

/**
 * Pure, IDE-free PSI text helpers shared by [PsiStructureWalker], [PsiReferenceCollector] and
 * [PsiUsageSearcher]. None of these touch the PSI runtime, a Document, or a read action — the
 * [PsiElement] parameter is read via plain Java reflection only — so they stay unit-testable
 * without a platform fixture.
 */

/**
 * Class names that the IntelliJ language plugins use for locals and parameters. Searching these
 * across `project` / `all` scope is wasteful (their binding cannot leak past their containing
 * file), so the usage searcher narrows them to the file. Using the simple name as a marker keeps
 * detection lang-agnostic at link time — no Java/Kotlin/JS PSI imports.
 */
val LOCAL_VARIABLE_LIKE: Set<String> = setOf(
    "PsiLocalVariable",
    "PsiParameter",
    "KtParameter",
    "KtDestructuringDeclarationEntry",
    "JSParameter",
)

/**
 * The simple class name of [element], falling back to the fully qualified name for anonymous or
 * synthetic classes whose simple name is empty.
 */
fun psiClassName(element: PsiElement): String =
    element.javaClass.simpleName.ifEmpty { element.javaClass.name }

/**
 * The first non-blank line of [text], trimmed. Empty when every line is blank. Used for declaration
 * previews — methods and classes can span hundreds of lines and the full text is useless without a
 * viewer.
 */
fun firstNonBlankLine(text: String): String =
    text.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
