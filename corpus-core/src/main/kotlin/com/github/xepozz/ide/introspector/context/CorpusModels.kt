package com.github.xepozz.ide.introspector.context

data class RawFile(
    val relativePath: String,
    val text: String,
)

fun interface FileSource {
    fun readAll(): List<RawFile>
}

data class CorpusEntry(
    val id: String,
    val frontmatter: Frontmatter,
    val body: String,
    val relativePath: String,
    val layer: CorpusLayer,
    val source: Source,
)

data class LoadResult(
    val entries: List<CorpusEntry>,
    val issues: List<ValidationIssue>,
)

data class MergeResult(
    val entries: List<CorpusEntry>,
    val issues: List<ValidationIssue>,
)
