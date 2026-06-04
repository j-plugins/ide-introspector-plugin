package com.github.xepozz.ide.introspector.core.context

object ContextServices {
    val retriever: ContextRetriever by lazy { ContextRetriever() }
}
