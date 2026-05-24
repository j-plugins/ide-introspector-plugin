package com.github.xepozz.ide.introspector.core

import com.github.xepozz.ide.introspector.model.LineColumn
import com.github.xepozz.ide.introspector.model.SymbolAtResponse
import com.github.xepozz.ide.introspector.model.SymbolInfo
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDocCommentBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReference
import com.intellij.psi.util.PsiTreeUtil

/**
 * Resolves the symbol at a given offset in a PSI file — the backing logic for
 * `psi.symbol_at`. Mirrors [PsiUsageSearcher.resolveTarget] in that it first tries to follow a
 * reference under the caret (Ctrl-click semantics) and otherwise walks up to the nearest
 * named declaration. The difference: this returns one compact [SymbolInfo] per call, not the
 * full Find Usages result.
 *
 * Caller MUST hold a platform read action (use `readActionBlocking { ... }`).
 *
 * Doc text collection uses the platform-stable `PsiDocCommentBase` hierarchy:
 *  - Java's `PsiDocComment implements PsiDocCommentBase`.
 *  - Kotlin's `KDoc` lives in the optional Kotlin plugin — we reach it via
 *    `PsiTreeUtil.getChildOfAnyType(target, PsiDocCommentBase::class.java)` which is
 *    interface-typed, so we don't take a hard dependency on Kotlin classes.
 */
object PsiSymbolResolver {

    /**
     * Resolve the symbol at [offset] in [psiFile]. Returns a fully-populated
     * [SymbolAtResponse] including the position the caller asked for and any warnings
     * about polyvariant resolution / no-target / etc.
     */
    fun resolveAt(
        psiFile: PsiFile,
        hostDocument: Document?,
        offset: Int,
        includeDoc: Boolean,
        truncateDocAt: Int,
    ): SymbolAtResponse {
        val fileUrl = psiFile.virtualFile?.url ?: ""
        val position = lineColOf(hostDocument, offset)
        val warnings = ArrayList<String>(2)

        val docTextLen = hostDocument?.textLength ?: psiFile.textLength
        if (offset > docTextLen) {
            warnings += "position past end of file"
            return SymbolAtResponse(
                fileUrl = fileUrl, offset = offset, position = position,
                symbol = null, warnings = warnings,
            )
        }

        // (1) Try to follow a reference under the caret. If we find one, the resolved
        // declaration becomes the target and we flag isReference=true.
        val (resolved, isReference) = resolveTargetAt(psiFile, offset, warnings)
            ?: return SymbolAtResponse(
                fileUrl = fileUrl, offset = offset, position = position,
                symbol = null, warnings = warnings.also {
                    if (warnings.isEmpty()) it += "no named symbol at position"
                },
            )

        val classified = PsiKindClassifier.classify(resolved)
        val (effectiveName, effectiveKind) = resolveSyntheticName(resolved, classified)
        val declarationFile = resolved.containingFile
        val declarationFileUrl = declarationFile?.virtualFile?.url ?: fileUrl
        val declarationDocument = declarationFile?.viewProvider?.document

        val docText = if (includeDoc) collectDocText(resolved, truncateDocAt) else null

        val symbol = SymbolInfo(
            name = effectiveName,
            kind = effectiveKind,
            fqn = classified.fqn,
            psiClass = resolved.javaClass.simpleName.ifEmpty { resolved.javaClass.name },
            declarationRange = PsiStructureWalker.textRangeInfoOf(resolved.textRange, declarationDocument),
            declarationFileUrl = declarationFileUrl,
            containingDeclarationName = enclosingDeclarationName(resolved),
            modifiers = classified.modifiers,
            returnType = classified.returnType,
            typeText = classified.typeText,
            isReference = isReference,
            docText = docText,
        )
        return SymbolAtResponse(
            fileUrl = fileUrl,
            offset = offset,
            position = position,
            symbol = symbol,
            warnings = warnings,
        )
    }

    /**
     * Returns (target, isReference) — null when nothing meaningful is at the offset.
     * Records polyvariant warnings into [warnings].
     *
     * Resolution order:
     *   1. Injected file under the caret → try `findReferenceAt` in the injected coordinate
     *      space first (SQL identifier inside a Kotlin string literal, regex inside a JS
     *      string, etc.). `PsiFile.findReferenceAt` only walks the receiver's own PSI tree —
     *      it does NOT recurse into injections — so we must call it explicitly on the
     *      injected file.
     *   2. Host file's `findReferenceAt` — classic Ctrl-click on a same-file usage.
     *   3. Walk up to the nearest [PsiNamedElement] from the injected (or host) leaf — caret
     *      on a declaration itself.
     */
    private fun resolveTargetAt(
        psiFile: PsiFile,
        offset: Int,
        warnings: MutableList<String>,
    ): Pair<PsiElement, Boolean>? {
        val project = psiFile.project
        val injManager = InjectedLanguageManager.getInstance(project)

        // (1) Injection-aware reference lookup: if a leaf inside an injected fragment sits at
        // the host offset, attempt to resolve a reference in the injected file's coordinate
        // space before falling back to the host file. This honours the plan's edge case #3.
        val injectedLeaf = injManager.findInjectedElementAt(psiFile, offset)
        if (injectedLeaf != null) {
            val injectedFile = injectedLeaf.containingFile
            if (injectedFile != null && injectedFile !== psiFile) {
                val injectedOffset = injectedLeaf.textRange?.startOffset?.let { startInInjected ->
                    // The leaf's textRange is already in the injected file's coordinates;
                    // bias by the difference between the host offset and the injected leaf's
                    // host-mapped offset to land at the exact caret column inside the injection.
                    val hostRangeOfLeaf = injManager.injectedToHost(injectedLeaf, injectedLeaf.textRange)
                    val delta = (offset - hostRangeOfLeaf.startOffset).coerceAtLeast(0)
                    (startInInjected + delta).coerceIn(0, injectedFile.textLength)
                } ?: 0
                val injectedRef = injectedFile.findReferenceAt(injectedOffset)
                if (injectedRef != null) {
                    val resolved = resolveReference(injectedRef, warnings)
                    if (resolved != null) return resolved to true
                }
            }
        }

        // (2) Host reference path: same as PsiUsageSearcher.resolveTarget.
        val ref: PsiReference? = psiFile.findReferenceAt(offset)
        if (ref != null) {
            val resolved = resolveReference(ref, warnings)
            if (resolved != null) return resolved to true
        }

        // (3) Declaration path: walk up to the nearest named ancestor. Prefer the injected
        // leaf when one exists (SQL token inside a Kotlin string).
        val leaf = injectedLeaf
            ?: psiFile.findElementAt(offset)
            ?: return null

        val named = PsiTreeUtil.getNonStrictParentOfType(leaf, PsiNamedElement::class.java)
            ?: return null
        return named to false
    }

    private fun resolveReference(ref: PsiReference, warnings: MutableList<String>): PsiElement? {
        return if (ref is PsiPolyVariantReference) {
            val all = ref.multiResolve(true).mapNotNull { it.element }
            if (all.size > 1) {
                warnings += "${all.size - 1} other resolutions available — use psi.get_references for the full set"
            }
            all.firstOrNull()
        } else {
            ref.resolve()
        }
    }

    /**
     * The classifier returns `name = null` for anonymous/synthetic PSI; for `psi.symbol_at`
     * we still want SOMETHING displayable when the platform gives us nothing. Returns the
     * adjusted (name, kind) pair.
     */
    private fun resolveSyntheticName(
        element: PsiElement,
        classified: PsiKindClassifier.Classified,
    ): Pair<String?, String> {
        // Anonymous Java class — surface "<anonymous>" rather than null.
        if (classified.kind == "class" && classified.name == null && element is PsiClass) {
            return "<anonymous>" to classified.kind
        }
        return classified.name to classified.kind
    }

    /**
     * Walk the children of [target] looking for any [PsiDocCommentBase] child — covers
     * Java's PsiDocComment, Kotlin's KDoc (KDoc implements PsiDocCommentBase since 2020.2),
     * and any other language that contributes one. Truncates to [truncateDocAt] chars.
     */
    private fun collectDocText(target: PsiElement, truncateDocAt: Int): String? {
        if (truncateDocAt == 0) return null
        val docComment = PsiTreeUtil.getChildOfType(target, PsiDocCommentBase::class.java)
            ?: return null
        val raw = try {
            docComment.text ?: return null
        } catch (_: Throwable) {
            return null
        }
        return if (raw.length <= truncateDocAt) raw
        else raw.substring(0, truncateDocAt) + "…"
    }

    private fun enclosingDeclarationName(element: PsiElement): String? {
        var node: PsiElement? = element.parent
        while (node != null && node !is PsiFile) {
            if (node is PsiNamedElement) {
                val name = node.name
                if (!name.isNullOrBlank()) return name
            }
            node = node.parent
        }
        return null
    }

    private fun lineColOf(document: Document?, offset: Int): LineColumn {
        if (document == null) return LineColumn(1, 1)
        val capped = offset.coerceIn(0, document.textLength)
        val line = document.getLineNumber(capped)
        val col = capped - document.getLineStartOffset(line)
        return LineColumn(line + 1, col + 1)
    }
}
