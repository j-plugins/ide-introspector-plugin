package com.github.xepozz.ide.introspector.context

class CorpusLoader(
    private val fileSource: FileSource,
    private val sourceCleaner: SourceCleaner = SourceCleaner(),
) {
    fun load(): LoadResult {
        val sortedFiles = fileSource.readAll().sortedBy { it.relativePath }
        val entries = mutableListOf<CorpusEntry>()
        val issues = mutableListOf<ValidationIssue>()
        for (file in sortedFiles) {
            loadFile(file, entries, issues)
        }
        return LoadResult(entries = entries, issues = issues)
    }

    private fun loadFile(
        file: RawFile,
        entries: MutableList<CorpusEntry>,
        issues: MutableList<ValidationIssue>,
    ) {
        val path = file.relativePath
        val layer = CorpusLayers.fromRelativePath(path)
        val parsed = FrontmatterParser.parse(file.text, path)
        val layerSource = CorpusLayers.sourceFor(layer)
        val body = bodyFor(layer, parsed.body)

        val frontmatter = parsed.frontmatter
        if (frontmatter == null) {
            if (layer == CorpusLayer.MANUAL) {
                issues += parsed.parseIssues
                issues += ValidationIssue(
                    severity = Severity.ERROR,
                    code = IssueCode.MISSING_FRONTMATTER,
                    message = "Missing frontmatter in $path",
                    sourcePath = path,
                )
                return
            }
            val derived = deriveFrontmatter(path, layer, layerSource, file.text)
            entries += CorpusEntry(
                id = derived.id,
                frontmatter = derived,
                body = body,
                relativePath = path,
                layer = layer,
                source = layerSource,
            )
            return
        }

        issues += parsed.parseIssues
        issues += FrontmatterValidator.validate(frontmatter, path)
        issues += layerIssues(frontmatter, layer, layerSource, path)
        entries += CorpusEntry(
            id = frontmatter.id,
            frontmatter = frontmatter,
            body = body,
            relativePath = path,
            layer = layer,
            source = layerSource,
        )
    }

    private fun layerIssues(
        frontmatter: Frontmatter,
        layer: CorpusLayer,
        layerSource: Source,
        path: String,
    ): List<ValidationIssue> =
        buildList {
            val declaredSource = frontmatter.source
            if (declaredSource != null && declaredSource != layerSource) {
                add(
                    ValidationIssue(
                        severity = Severity.WARNING,
                        code = IssueCode.LAYER_SOURCE_MISMATCH,
                        message = "Frontmatter source $declaredSource does not match layer source $layerSource in $path",
                        sourcePath = path,
                    )
                )
            }
            if (layer == CorpusLayer.UNKNOWN) {
                add(
                    ValidationIssue(
                        severity = Severity.WARNING,
                        code = IssueCode.UNKNOWN_LAYER,
                        message = "Unknown corpus layer for path $path",
                        sourcePath = path,
                    )
                )
            }
            if (layer == CorpusLayer.EXAMPLES && !matchesExamplePath(path)) {
                add(
                    ValidationIssue(
                        severity = Severity.WARNING,
                        code = IssueCode.EXAMPLE_PATH_MISMATCH,
                        message = "Example path $path does not match generated/examples/plugins/<pluginId>/<ep>.md",
                        sourcePath = path,
                    )
                )
            }
        }

    private fun deriveFrontmatter(
        path: String,
        layer: CorpusLayer,
        layerSource: Source,
        rawText: String,
    ): Frontmatter {
        val subPath = normalize(path).removePrefix("generated/").removeSuffix(".md")
        val id = when (layer) {
            CorpusLayer.SDK_PLATFORM -> "sdk." + subPath.removePrefix("sdk-platform/").replace('/', '.')
            else -> subPath.replace('/', '.')
        }
        val slug = subPath.substringAfterLast('/')
        val title = firstHeading(rawText) ?: humanize(slug)
        return Frontmatter(
            id = id,
            title = title,
            source = layerSource,
            kind = Kind.REFERENCE,
            tags = deriveTags(slug),
        )
    }

    private fun firstHeading(rawText: String): String? =
        rawText.lineSequence()
            .firstOrNull { it.startsWith("# ") }
            ?.removePrefix("# ")
            ?.trim()
            ?.ifBlank { null }

    private fun humanize(slug: String): String =
        slug.split('-')
            .filter { it.isNotBlank() }
            .joinToString(" ") { it.replaceFirstChar { character -> character.uppercase() } }
            .ifBlank { "Untitled" }

    private fun deriveTags(slug: String): List<String> {
        val derived = slug.split('-')
            .filter { it.length >= 3 && it != "the" && it != "and" }
            .distinct()
            .take(5)
        return listOf("sdk-platform") + derived
    }

    private fun bodyFor(layer: CorpusLayer, body: String): String =
        when (layer) {
            CorpusLayer.EXAMPLES -> cleanKotlinFences(body)
            else -> body
        }

    private fun cleanKotlinFences(body: String): String =
        KOTLIN_FENCE.replace(body) { match ->
            val opening = match.groupValues[1]
            val code = match.groupValues[2]
            val closing = match.groupValues[3]
            opening + sourceCleaner.clean(code) + closing
        }

    private fun matchesExamplePath(path: String): Boolean =
        EXAMPLE_PATH.matches(normalize(path))

    private fun normalize(path: String): String =
        path
            .replace('\\', '/')
            .removePrefix("./")
            .removePrefix("/")

    private companion object {
        val KOTLIN_FENCE = Regex("""(```kotlin\r?\n)([\s\S]*?)(\r?\n```)""")
        val EXAMPLE_PATH = Regex("""generated/examples/plugins/[^/]+/[^/]+\.md""")
    }
}
