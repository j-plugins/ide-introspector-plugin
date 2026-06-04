package com.github.xepozz.ide.introspector.context

data class GeneratedDoc(val relativePath: String, val content: String)

class SdkDocsConverter(
    private val baseUrl: String = "https://plugins.jetbrains.com/docs/intellij",
    private val maxChunkChars: Int = 6_000,
    private val minSplitChars: Int = 2_000,
) {
    fun convert(llmsText: String, selectedTitles: Set<String>): List<GeneratedDoc> {
        val usedTopSlugs = mutableSetOf<String>()
        val docs = mutableListOf<GeneratedDoc>()
        for (topic in LlmsTopicSplitter.split(llmsText).filter { it.title in selectedTitles }) {
            val slug = uniqueSlug(Slugs.slugify(topic.title), usedTopSlugs)
            val body = rewriteLinks(topic.body)
            val tree = HeadingTreeParser.parse(body)
            val total = tree.intro.length + tree.roots.sumOf { subtreeChars(it) }
            val content = if (total <= maxChunkChars || tree.roots.isEmpty()) {
                buildString {
                    appendLine("# ${topic.title}")
                    appendLine()
                    appendLine(body)
                }
            } else {
                buildString {
                    appendLine("# ${topic.title}")
                    appendLine()
                    if (tree.intro.isNotBlank()) {
                        appendLine(tree.intro)
                        appendLine()
                    }
                    append(renderChildren(tree.roots, slug, "sdk.$slug", docs))
                }
            }
            docs += GeneratedDoc("$slug.md", content.trimEnd() + "\n")
        }
        return docs
    }

    private fun renderChildren(
        children: List<HeadingNode>,
        parentPath: String,
        parentId: String,
        sink: MutableList<GeneratedDoc>,
    ): String = buildString {
        val usedSlugs = mutableSetOf<String>()
        for (child in children) {
            val title = child.title.ifBlank { firstMeaningfulLine(child.body) }
            if (subtreeChars(child) > minSplitChars) {
                val slug = uniqueSlug(Slugs.slugify(title), usedSlugs)
                val path = "$parentPath/$slug"
                val id = "$parentId.$slug"
                appendLine("${"#".repeat(child.level)} $title ($id)")
                emitSection(child, title, path, id, sink)
            } else {
                append(inline(listOf(child)))
            }
        }
    }

    private fun emitSection(node: HeadingNode, title: String, path: String, id: String, sink: MutableList<GeneratedDoc>) {
        val content = buildString {
            appendLine("# $title")
            appendLine()
            if (node.body.isNotBlank()) {
                appendLine(node.body)
                appendLine()
            }
            if (subtreeChars(node) > maxChunkChars) {
                append(renderChildren(node.children, path, id, sink))
            } else {
                append(inline(node.children))
            }
        }
        sink += GeneratedDoc("$path.md", content.trimEnd() + "\n")
    }

    private fun inline(nodes: List<HeadingNode>): String = buildString {
        for (node in nodes) {
            val title = node.title.ifBlank { firstMeaningfulLine(node.body) }
            appendLine("${"#".repeat(node.level)} $title")
            appendLine()
            if (node.body.isNotBlank()) {
                appendLine(node.body)
                appendLine()
            }
            append(inline(node.children))
        }
    }

    private fun subtreeChars(node: HeadingNode): Int =
        node.title.length + node.body.length + 4 + node.children.sumOf { subtreeChars(it) }

    private fun firstMeaningfulLine(body: String): String {
        val line = body.lineSequence().map { it.trim() }.firstOrNull { it.isNotBlank() } ?: return "section"
        return line.trim('*', '`', '#', '-', ' ', '>', '<').take(60).trim().ifBlank { "section" }
    }

    private fun uniqueSlug(base: String, used: MutableSet<String>): String {
        val candidate = base.ifEmpty { "section" }
        if (used.add(candidate)) return candidate
        var index = 2
        while (!used.add("$candidate-$index")) index++
        return "$candidate-$index"
    }

    private fun rewriteLinks(body: String): String =
        RELATIVE_LINK.replace(body) { "](${baseUrl}/${it.groupValues[1]})" }

    private companion object {
        val RELATIVE_LINK = Regex("""\]\((?!https?://|#)([A-Za-z0-9._-]+\.html(?:#[^)\s]*)?)\)""")
    }
}
