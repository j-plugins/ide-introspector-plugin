package com.github.xepozz.ide.introspector.util

import com.intellij.openapi.diagnostic.thisLogger

object ReflectionAccess {
    fun readMethod(target: Any, name: String): Any? {
        val method = target.javaClass.methods.firstOrNull { it.name == name && it.parameterCount == 0 }
            ?: return null
        return try {
            method.isAccessible = true
            method.invoke(target)
        } catch (throwable: Throwable) {
            logFailure("method", target, name, throwable)
            null
        }
    }

    fun readField(target: Any, name: String): Any? {
        var currentClass: Class<*>? = target.javaClass
        while (currentClass != null) {
            val field = currentClass.declaredFields.firstOrNull { it.name == name }
            if (field != null) {
                return try {
                    field.isAccessible = true
                    field.get(target)
                } catch (throwable: Throwable) {
                    logFailure("field", target, name, throwable)
                    null
                }
            }
            currentClass = currentClass.superclass
        }
        return null
    }

    private fun logFailure(kind: String, target: Any, name: String, throwable: Throwable) {
        thisLogger().warn(
            "Reflective $kind read '$name' on ${target.javaClass.name} failed: ${throwable.message}",
            throwable,
        )
    }
}
