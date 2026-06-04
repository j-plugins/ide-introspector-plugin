package com.github.xepozz.ide.introspector.context

data class Frontmatter(
    val id: String,
    val title: String?,
    val source: Source?,
    val kind: Kind?,
    val state: State = State.VERIFIED,
    val description: String? = null,
    val whenToUse: String? = null,
    val verifiedAgainstBuild: String? = null,
    val tags: List<String> = emptyList(),
    val relatedExtensionPoints: List<String> = emptyList(),
    val relatedTools: List<String> = emptyList(),
    val plugin: String? = null,
    val extensionPoint: String? = null,
    val unknownKeys: List<String> = emptyList(),
    val idDerivedFromPath: Boolean = false,
)

data class ParsedDocument(
    val frontmatter: Frontmatter?,
    val body: String,
    val parseIssues: List<ValidationIssue>,
)
