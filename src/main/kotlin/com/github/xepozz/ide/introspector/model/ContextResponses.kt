package com.github.xepozz.ide.introspector.model

import kotlinx.serialization.Serializable

@Serializable
data class SkillRef(
    val id: String,
    val title: String,
    val kind: String,
    val tags: List<String>,
    val tokenEstimate: Int,
)

@Serializable
data class SkillListResponse(
    val status: String,
    val skills: List<SkillRef> = emptyList(),
    val total: Int = 0,
    val appliedTag: String? = null,
    val reason: String? = null,
)

@Serializable
data class SearchHit(
    val id: String,
    val title: String,
    val kind: String,
    val source: String,
    val score: Double,
    val tokenEstimate: Int,
    val matchedTerms: List<String>,
)

@Serializable
data class ContextSearchResponse(
    val status: String,
    val query: String,
    val hits: List<SearchHit> = emptyList(),
    val total: Int = 0,
    val reason: String? = null,
)

@Serializable
data class ContextSectionResponse(
    val status: String,
    val id: String? = null,
    val title: String? = null,
    val kind: String? = null,
    val body: String? = null,
    val returnedTokens: Int = 0,
    val truncated: Boolean = false,
    val nextOffset: Int? = null,
    val reason: String? = null,
)
