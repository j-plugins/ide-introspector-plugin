package com.github.xepozz.ide.introspector.core.internal

import com.github.xepozz.ide.introspector.util.ReflectionAccess
import com.intellij.ide.plugins.IdeaPluginDescriptor

object ContainerDescriptorReader {

    fun readContainer(descriptor: IdeaPluginDescriptor, getterName: String): Any? =
        ReflectionAccess.readMethod(descriptor, getterName)

    inline fun <T> collectFromContainers(
        descriptor: IdeaPluginDescriptor,
        areaGetters: List<Pair<String, String>>,
        fieldName: String,
        crossinline map: (element: Any, areaTag: String) -> T?,
    ): List<T> {
        val out = mutableListOf<T>()
        for ((areaTag, getterName) in areaGetters) {
            val container = readContainer(descriptor, getterName) ?: continue
            for (element in readElementList(container, fieldName)) {
                out += map(element, areaTag) ?: continue
            }
        }
        return out
    }

    fun readElementList(container: Any, fieldName: String): List<Any> {
        val raw = ReflectionAccess.readField(container, fieldName)
        return (raw as? List<*>)?.filterNotNull().orEmpty()
    }
}
