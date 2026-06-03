package com.github.xepozz.ide.introspector.util

fun String.simpleClassName(): String = substringAfterLast('.').substringAfterLast('$')
