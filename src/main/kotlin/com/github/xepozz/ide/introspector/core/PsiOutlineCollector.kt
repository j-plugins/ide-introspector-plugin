package com.github.xepozz.ide.introspector.core

import com.github.xepozz.ide.introspector.model.GetOutlineResponse
import com.github.xepozz.ide.introspector.model.OutlineNode
import com.intellij.ide.structureView.StructureViewBuilder
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.ide.util.treeView.smartTree.NodeProvider
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.lang.LanguageStructureViewBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

/**
 * Builds the outline (declaration tree) for a [PsiFile] using IntelliJ's
 * [LanguageStructureViewBuilder]. Backs `psi.get_outline`.
 *
 * StructureViewBuilder is the per-language extension that powers the Structure tool window
 * — every language plugin (Java, Kotlin, JSON, YAML, …) contributes its own. Walking the
 * resulting `StructureViewModel` instead of the raw PSI tree gives us the same declaration
 * set the IDE shows in its sidebar, which is exactly the shape an agent wants when asking
 * "what methods are in this file?".
 *
 * Three reasons we don't fall back to a per-language hand-rolled walker:
 *  1. Languages outside Java/Kotlin (JSON, YAML, HTML, Properties) all have
 *     StructureViewBuilders out of the box — free coverage.
 *  2. Per-language structure ordering (e.g. Kotlin shows top-level functions before classes
 *     by default) is preserved.
 *  3. Anonymous classes / lambdas are excluded by the builder's natural taxonomy — same as
 *     the Structure tool window, same as what the agent expects.
 *
 * Caller MUST hold a platform read action.
 *
 * Limits:
 *  - [maxNodes] caps the TOTAL count across the recursive tree. When hit, returns the prefix
 *    walked so far + `truncated=true` + a warning.
 *  - [maxDepth] caps recursion depth. Per spec: "Max outline depth. Default 6."
 *  - When no [StructureViewBuilder] is registered for the file's language (binary file,
 *    obscure language plugin not loaded), returns empty nodes + warning.
 */
object PsiOutlineCollector {

    fun collect(
        psiFile: PsiFile,
        includeFields: Boolean,
        includeInherited: Boolean,
        maxDepth: Int,
        maxNodes: Int,
    ): GetOutlineResponse {
        val fileUrl = psiFile.virtualFile?.url ?: ""
        val fileType = psiFile.fileType.name
        val language = psiFile.language.id

        val builder = LanguageStructureViewBuilder.getInstance().getStructureViewBuilder(psiFile)
        if (builder == null) {
            return GetOutlineResponse(
                fileUrl = fileUrl,
                fileType = fileType,
                language = language,
                nodes = emptyList(),
                nodeCount = 0,
                truncated = false,
                warnings = listOf("No StructureViewBuilder for fileType=$fileType"),
            )
        }

        // Only tree-based builders expose a walkable model; some custom builders (e.g.
        // FileStructurePopupBuilder for non-tree views) return a TextEditorBasedStructureViewModel
        // wrapper. We require TreeBased to keep recursion logic simple and reliable.
        if (builder !is TreeBasedStructureViewBuilder) {
            return GetOutlineResponse(
                fileUrl = fileUrl,
                fileType = fileType,
                language = language,
                nodes = emptyList(),
                nodeCount = 0,
                truncated = false,
                warnings = listOf(
                    "StructureViewBuilder for fileType=$fileType is not tree-based (${builder.javaClass.simpleName})",
                ),
            )
        }

        val model: StructureViewModel = try {
            builder.createStructureViewModel(null)
        } catch (e: Throwable) {
            return GetOutlineResponse(
                fileUrl = fileUrl,
                fileType = fileType,
                language = language,
                nodes = emptyList(),
                nodeCount = 0,
                truncated = false,
                warnings = listOf("Failed to create StructureViewModel: ${e.javaClass.simpleName}: ${e.message}"),
            )
        }

        try {
            // Enumerate NodeProviders for the model. JavaInheritedMembersNodeProvider (and the
            // Kotlin equivalent) ride this surface — when `includeInherited=true` we invoke each
            // provider on every class node to fold its contributed children alongside the
            // structural ones. When the model contributes none (most non-Java languages), the
            // toggle silently degrades to "no extra members" without surprise.
            val nodeProviders: List<NodeProvider<TreeElement>> = if (includeInherited) {
                @Suppress("UNCHECKED_CAST")
                try {
                    // `getNodeProviders()` was added to StructureViewModel as a default method —
                    // not all implementations override it, and on older models it may not even
                    // be exposed as a synthetic Kotlin property. Reflective access keeps us
                    // tolerant of both shapes.
                    extractNodeProviders(model) as List<NodeProvider<TreeElement>>
                } catch (_: Throwable) {
                    emptyList()
                }
            } else emptyList()

            val ctx = WalkContext(
                includeFields = includeFields,
                includeInherited = includeInherited,
                maxDepth = maxDepth,
                maxNodes = maxNodes,
                nodeProviders = nodeProviders,
            )
            val rootChildren = model.root.children
            val outline = ArrayList<OutlineNode>(rootChildren.size)
            for (child in rootChildren) {
                if (ctx.exhausted()) {
                    ctx.truncated = true
                    break
                }
                val node = walk(child, depth = 0, ctx = ctx) ?: continue
                outline += node
            }
            val warnings = ArrayList<String>(2)
            if (ctx.truncated) warnings += "outline truncated at $maxNodes nodes"
            if (includeInherited && nodeProviders.isEmpty()) {
                warnings += "includeInherited=true requested but the structure view contributed no NodeProviders for fileType=$fileType"
            }
            return GetOutlineResponse(
                fileUrl = fileUrl,
                fileType = fileType,
                language = language,
                nodes = outline,
                nodeCount = ctx.emitted,
                truncated = ctx.truncated,
                warnings = warnings,
            )
        } finally {
            // StructureViewModel implementations may hold tree caches / listeners; per the
            // Structure View docs, callers should dispose() once they're done.
            try { model.dispose() } catch (_: Throwable) {}
        }
    }

    private class WalkContext(
        val includeFields: Boolean,
        val includeInherited: Boolean,
        val maxDepth: Int,
        val maxNodes: Int,
        val nodeProviders: List<NodeProvider<TreeElement>> = emptyList(),
        var emitted: Int = 0,
        var truncated: Boolean = false,
    ) {
        fun exhausted(): Boolean = emitted >= maxNodes
        fun tick() { emitted++ }
    }

    private fun walk(element: TreeElement, depth: Int, ctx: WalkContext): OutlineNode? {
        if (ctx.exhausted()) {
            ctx.truncated = true
            return null
        }
        val psi = (element as? StructureViewTreeElement)?.value as? PsiElement
            ?: return null   // skip non-PSI elements (e.g. JSON property nodes — but JsonStructureView wraps PsiElement)

        val classified = PsiKindClassifier.classify(psi)
        val kind = classified.kind

        // includeFields=false drops field/property leaves (still walks deeper if they happen
        // to have children — usually they don't).
        if (!ctx.includeFields && (kind == "field" || kind == "property")) return null

        // Name fallback: anonymous classes and synthetic PSI may have no name. The platform
        // structure view shows them with a labelled presentation — we adopt the simple name
        // from PsiKindClassifier and fall back to the PSI class name. We don't yet have a
        // displayable name, so use "<unnamed>" as a last resort to keep the schema invariant.
        val displayName = classified.name ?: psi.let {
            (element as? StructureViewTreeElement)?.presentation?.presentableText
        } ?: "<unnamed>"

        ctx.tick()

        val children = if (depth + 1 >= ctx.maxDepth) emptyList() else {
            val out = ArrayList<OutlineNode>(8)
            try {
                for (child in element.children) {
                    if (ctx.exhausted()) {
                        ctx.truncated = true
                        break
                    }
                    val node = walk(child, depth + 1, ctx) ?: continue
                    out += node
                }
                // includeInherited=true: ask each NodeProvider (e.g.
                // JavaInheritedMembersNodeProvider) for additional children of THIS element.
                // The provider returns inherited methods / fields contributed from supertypes;
                // we fold them into the same children list so the agent sees one unified node
                // set per class.
                if (ctx.includeInherited && ctx.nodeProviders.isNotEmpty()) {
                    for (provider in ctx.nodeProviders) {
                        if (ctx.exhausted()) {
                            ctx.truncated = true
                            break
                        }
                        val contributed: Collection<TreeElement> = try {
                            provider.provideNodes(element)
                        } catch (_: Throwable) {
                            continue
                        }
                        for (extra in contributed) {
                            if (ctx.exhausted()) {
                                ctx.truncated = true
                                break
                            }
                            val node = walk(extra, depth + 1, ctx) ?: continue
                            out += node
                        }
                    }
                }
            } catch (_: Throwable) {
                // A misbehaving structure-view contributor (typically Vue/PHP custom views).
                // Keep what we have — partial result beats nothing.
            }
            out
        }

        return OutlineNode(
            name = displayName,
            kind = kind,
            fqn = classified.fqn,
            psiClass = psi.javaClass.simpleName.ifEmpty { psi.javaClass.name },
            declarationRange = PsiStructureWalker.textRangeInfoOf(psi.textRange, psi.containingFile?.viewProvider?.document),
            modifiers = classified.modifiers,
            returnType = classified.returnType,
            typeText = classified.typeText,
            children = children,
        )
    }

    /**
     * `StructureViewModel.getNodeProviders()` is a default Java method (added 2017). We invoke
     * it reflectively to stay tolerant of implementations that don't override it cleanly and to
     * avoid binding to its raw-typed generics signature from Kotlin.
     */
    private fun extractNodeProviders(model: StructureViewModel): Collection<NodeProvider<*>> {
        val method = try {
            model.javaClass.getMethod("getNodeProviders")
        } catch (_: NoSuchMethodException) {
            return emptyList()
        }
        val result = try {
            method.invoke(model)
        } catch (_: Throwable) {
            return emptyList()
        } ?: return emptyList()
        @Suppress("UNCHECKED_CAST")
        return (result as? Collection<NodeProvider<*>>) ?: emptyList()
    }
}
