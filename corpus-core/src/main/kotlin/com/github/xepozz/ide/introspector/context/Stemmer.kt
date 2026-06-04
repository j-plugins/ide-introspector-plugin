package com.github.xepozz.ide.introspector.context

fun interface Stemmer {
    fun stem(token: String): String
}
