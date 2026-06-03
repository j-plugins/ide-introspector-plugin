package com.github.xepozz.ide.introspector.util

import com.intellij.openapi.diagnostic.thisLogger

object ReflectionAccess {
    fun readMethod(target: Any, vararg names: String): Any? {
        for (name in names) {
            val method = target.javaClass.methods.firstOrNull { it.name == name && it.parameterCount == 0 }
                ?: continue
            return try {
                method.isAccessible = true
                method.invoke(target)
            } catch (throwable: Throwable) {
                logFailure("method", target, name, throwable)
                null
            }
        }
        return null
    }

    fun readField(target: Any, vararg names: String): Any? {
        for (name in names) {
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
        }
        return null
    }

    fun readEnumName(target: Any, name: String): String? = (readField(target, name) as? Enum<*>)?.name

    private fun logFailure(kind: String, target: Any, name: String, throwable: Throwable) {
        thisLogger().warn(
            "Reflective $kind read '$name' on ${target.javaClass.name} failed: ${throwable.message}",
            throwable,
        )
    }
}
