package com.github.xepozz.ide.introspector.model

import kotlinx.serialization.Serializable

@Serializable
data class IndexingStatusResponse(
    val projectName: String,
    val indexing: Boolean,
    val ready: Boolean,
)
