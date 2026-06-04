package com.github.xepozz.ide.introspector.context

data class GeneratedDoc(val relativePath: String, val content: String)

class SdkDocsConverter(
    private val build: String,
    private val baseUrl: String = "https://plugins.jetbrains.com/docs/intellij",
) {
    fun convert(llmsText: String, selectedTitles: Set<String>): List<GeneratedDoc> {
        val usedSlugs = mutableSetOf<String>()
        return LlmsTopicSplitter.split(llmsText)
            .filter { it.title in selectedTitles }
            .map { topic ->
                val slug = uniqueSlug(Slugs.slugify(topic.title), usedSlugs)
                GeneratedDoc("$slug.md", render(topic, slug))
            }
    }

    private fun uniqueSlug(base: String, used: MutableSet<String>): String {
        val candidate = base.ifEmpty { "topic" }
        if (used.add(candidate)) return candidate
        var index = 2
        while (!used.add("$candidate-$index")) index++
        return "$candidate-$index"
    }

    private fun render(topic: LlmsTopic, slug: String): String = buildString {
        appendLine("---")
        appendLine("id: sdk.$slug")
        appendLine("title: ${topic.title}")
        appendLine("source: generated")
        appendLine("kind: reference")
        appendLine("verifiedAgainstBuild: $build")
        appendLine("tags: ${renderTags(slug)}")
        appendLine("---")
        appendLine(rewriteLinks(topic.body))
        appendLine()
        appendLine("> Source: IntelliJ Platform SDK docs — ${topic.title} (build $build). $baseUrl/llms.txt")
    }

    private fun renderTags(slug: String): String {
        val derived = slug.split('-')
            .filter { it.length >= 3 && it != "the" && it != "and" }
            .distinct()
            .take(5)
        return (listOf("sdk-platform") + derived).joinToString(", ", prefix = "[", postfix = "]")
    }

    private fun rewriteLinks(body: String): String =
        RELATIVE_LINK.replace(body) { "](${baseUrl}/${it.groupValues[1]})" }

    private companion object {
        val RELATIVE_LINK = Regex("""\]\((?!https?://|#)([A-Za-z0-9._-]+\.html(?:#[^)\s]*)?)\)""")
    }
}
