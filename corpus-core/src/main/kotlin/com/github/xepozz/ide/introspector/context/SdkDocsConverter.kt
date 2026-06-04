package com.github.xepozz.ide.introspector.context

data class GeneratedDoc(val relativePath: String, val content: String)

class SdkDocsConverter(
    private val build: String,
    private val baseUrl: String = "https://plugins.jetbrains.com/docs/intellij",
    private val maxChunkChars: Int = 6_000,
) {
    fun convert(llmsText: String, selectedTitles: Set<String>): List<GeneratedDoc> {
        val usedTopSlugs = mutableSetOf<String>()
        val docs = mutableListOf<GeneratedDoc>()
        for (topic in LlmsTopicSplitter.split(llmsText).filter { it.title in selectedTitles }) {
            val slug = uniqueSlug(Slugs.slugify(topic.title), usedTopSlugs)
            val body = rewriteLinks(topic.body)
            val tree = if (body.length > maxChunkChars) HeadingTreeParser.parse(body) else null
            if (tree == null || tree.roots.isEmpty()) {
                docs += GeneratedDoc("$slug.md", renderSingle("sdk.$slug", topic.title, slug, body))
            } else {
                val sections = assign(tree.roots, slug, "sdk.$slug")
                docs += GeneratedDoc("$slug.md", renderMainIndex("sdk.$slug", topic.title, slug, tree.intro, sections))
                collectSectionDocs(topic.title, sections, docs)
            }
        }
        return docs
    }

    private fun assign(nodes: List<HeadingNode>, parentPath: String, parentId: String): List<Section> {
        val usedSlugs = mutableSetOf<String>()
        return nodes.map { node ->
            val title = node.title.ifBlank { firstMeaningfulLine(node.body) }
            val slug = uniqueSlug(Slugs.slugify(title), usedSlugs)
            val path = "$parentPath/$slug"
            val id = "$parentId.$slug"
            Section(title, slug, path, id, node.level, node.body, assign(node.children, path, id))
        }
    }

    private fun collectSectionDocs(rootTitle: String, sections: List<Section>, sink: MutableList<GeneratedDoc>) {
        for (section in sections) {
            sink += GeneratedDoc("${section.path}.md", renderSection(rootTitle, section))
            collectSectionDocs(rootTitle, section.children, sink)
        }
    }

    private fun renderSingle(id: String, title: String, slug: String, body: String): String = buildString {
        append(frontmatter(id, title, tagsFor(slug)))
        appendLine(body)
        appendLine()
        appendLine(footer(title))
    }

    private fun renderMainIndex(
        id: String,
        title: String,
        slug: String,
        intro: String,
        sections: List<Section>,
    ): String = buildString {
        append(frontmatter(id, title, tagsFor(slug)))
        if (intro.isNotBlank()) {
            appendLine(intro)
            appendLine()
        }
        appendOutline(sections)
        appendLine()
        appendLine(footer(title))
    }

    private fun renderSection(rootTitle: String, section: Section): String = buildString {
        append(frontmatter(section.id, "$rootTitle: ${section.title}", tagsFor(section.slug)))
        if (section.body.isNotBlank()) {
            appendLine(section.body)
            appendLine()
        }
        if (section.children.isNotEmpty()) {
            appendOutline(section.children)
        }
    }

    private fun StringBuilder.appendOutline(sections: List<Section>) {
        for (section in sections) {
            appendLine("${"#".repeat(section.level)} ${section.title} (${section.path}.md)")
            appendOutline(section.children)
        }
    }

    private fun frontmatter(id: String, title: String, tags: String): String = buildString {
        appendLine("---")
        appendLine("id: $id")
        appendLine("title: $title")
        appendLine("source: generated")
        appendLine("kind: reference")
        appendLine("verifiedAgainstBuild: $build")
        appendLine("tags: $tags")
        appendLine("---")
    }

    private fun footer(title: String): String =
        "> Source: IntelliJ Platform SDK docs — $title (build $build). $baseUrl/llms.txt"

    private fun tagsFor(slug: String): String {
        val derived = slug.split('-')
            .filter { it.length >= 3 && it != "the" && it != "and" }
            .distinct()
            .take(5)
        return (listOf("sdk-platform") + derived).joinToString(", ", prefix = "[", postfix = "]")
    }

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

    private class Section(
        val title: String,
        val slug: String,
        val path: String,
        val id: String,
        val level: Int,
        val body: String,
        val children: List<Section>,
    )

    private companion object {
        val RELATIVE_LINK = Regex("""\]\((?!https?://|#)([A-Za-z0-9._-]+\.html(?:#[^)\s]*)?)\)""")
    }
}
