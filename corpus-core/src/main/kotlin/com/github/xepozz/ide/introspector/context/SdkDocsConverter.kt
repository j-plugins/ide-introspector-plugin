package com.github.xepozz.ide.introspector.context

data class GeneratedDoc(val relativePath: String, val content: String)

class SdkDocsConverter(
    private val build: String,
    private val baseUrl: String = "https://plugins.jetbrains.com/docs/intellij",
    private val maxChunkChars: Int = 6_000,
    private val deepestHeadingLevel: Int = 4,
) {
    fun convert(llmsText: String, selectedTitles: Set<String>): List<GeneratedDoc> {
        val usedSlugs = mutableSetOf<String>()
        return LlmsTopicSplitter.split(llmsText)
            .filter { it.title in selectedTitles }
            .flatMap { topic ->
                val slug = uniqueSlug(Slugs.slugify(topic.title), usedSlugs)
                emit(
                    rootTitle = topic.title,
                    title = topic.title,
                    slug = slug,
                    body = rewriteLinks(topic.body),
                    pathPrefix = "",
                    parentId = ROOT_ID,
                    level = 2,
                )
            }
    }

    private fun emit(
        rootTitle: String,
        title: String,
        slug: String,
        body: String,
        pathPrefix: String,
        parentId: String,
        level: Int,
    ): List<GeneratedDoc> {
        val id = "$parentId.$slug"
        val displayTitle = if (parentId == ROOT_ID) title else "$rootTitle: $title"
        val relativePath = "$pathPrefix$slug.md"

        val split = splitByDeepestUsefulLevel(body, level)
        if (split == null) {
            return listOf(GeneratedDoc(relativePath, renderLeaf(id, displayTitle, slug, parentId, body)))
        }

        val usedSubSlugs = mutableSetOf<String>()
        val children = split.split.sections.map { section ->
            val childTitle = section.title.ifBlank { firstMeaningfulLine(section.body) }
            val subSlug = uniqueSlug(Slugs.slugify(childTitle), usedSubSlugs)
            Child(childTitle, subSlug, "$id.$subSlug", section.body)
        }
        val childDocs = children.flatMap { child ->
            emit(rootTitle, child.title, child.slug, child.body, "$pathPrefix$slug/", id, split.level + 1)
        }
        val indexDoc = GeneratedDoc(
            relativePath,
            renderIndex(id, displayTitle, slug, parentId, split.split.intro, children),
        )
        return childDocs + indexDoc
    }

    private fun splitByDeepestUsefulLevel(body: String, fromLevel: Int): LeveledSplit? {
        if (body.length <= maxChunkChars) return null
        var level = fromLevel
        while (level <= deepestHeadingLevel) {
            val split = MarkdownSections.split(body, level)
            if (split.sections.size >= 2) return LeveledSplit(level, split)
            level++
        }
        return null
    }

    private fun renderLeaf(id: String, title: String, slug: String, parentId: String, body: String): String =
        buildString {
            append(frontmatter(id, title, tagsFor(slug)))
            appendBreadcrumb(parentId)
            appendLine(body)
            appendLine()
            appendLine(footer(title))
        }

    private fun renderIndex(
        id: String,
        title: String,
        slug: String,
        parentId: String,
        intro: String,
        children: List<Child>,
    ): String = buildString {
        append(frontmatter(id, title, tagsFor(slug)))
        appendBreadcrumb(parentId)
        if (intro.isNotBlank()) {
            appendLine(intro)
            appendLine()
        }
        appendLine("## Subtopics")
        appendLine()
        for (child in children) {
            appendLine("- ${child.title} — `${child.id}`")
        }
        appendLine()
        appendLine(footer(title))
    }

    private fun StringBuilder.appendBreadcrumb(parentId: String) {
        if (parentId != ROOT_ID) {
            appendLine("Part of `$parentId`.")
            appendLine()
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

    private fun uniqueSlug(base: String, used: MutableSet<String>): String {
        val candidate = base.ifEmpty { "topic" }
        if (used.add(candidate)) return candidate
        var index = 2
        while (!used.add("$candidate-$index")) index++
        return "$candidate-$index"
    }

    private fun firstMeaningfulLine(body: String): String {
        val line = body.lineSequence().map { it.trim() }.firstOrNull { it.isNotBlank() } ?: return "section"
        return line.trim('*', '`', '#', '-', ' ', '>', '<').take(60).trim().ifBlank { "section" }
    }

    private fun rewriteLinks(body: String): String =
        RELATIVE_LINK.replace(body) { "](${baseUrl}/${it.groupValues[1]})" }

    private class Child(val title: String, val slug: String, val id: String, val body: String)

    private class LeveledSplit(val level: Int, val split: MarkdownSplit)

    private companion object {
        const val ROOT_ID = "sdk"
        val RELATIVE_LINK = Regex("""\]\((?!https?://|#)([A-Za-z0-9._-]+\.html(?:#[^)\s]*)?)\)""")
    }
}
