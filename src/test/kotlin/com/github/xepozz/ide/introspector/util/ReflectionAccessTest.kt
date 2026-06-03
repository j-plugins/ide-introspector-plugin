package com.github.xepozz.ide.introspector.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

open class ReflectionAccessTestBase {
    @JvmField
    val baseField: String = "base-value"
}

class ReflectionAccessTestChild : ReflectionAccessTestBase() {
    @JvmField
    val childField: Int = 42

    fun label(): String = "label"

    fun boom(): String = throw IllegalStateException("boom")
}

class ReflectionAccessTest {

    @Test
    fun `readField reads a declared field`() {
        assertEquals(42, ReflectionAccess.readField(ReflectionAccessTestChild(), "childField"))
    }

    @Test
    fun `readField walks up to a superclass field`() {
        assertEquals("base-value", ReflectionAccess.readField(ReflectionAccessTestChild(), "baseField"))
    }

    @Test
    fun `readField returns null for a missing field`() {
        assertNull(ReflectionAccess.readField(ReflectionAccessTestChild(), "absentField"))
    }

    @Test
    fun `readMethod invokes a no-arg method`() {
        assertEquals("label", ReflectionAccess.readMethod(ReflectionAccessTestChild(), "label"))
    }

    @Test
    fun `readMethod returns null for a missing method`() {
        assertNull(ReflectionAccess.readMethod(ReflectionAccessTestChild(), "absentMethod"))
    }

    @Test
    fun `readMethod swallows an exception thrown by the target method and returns null`() {
        assertNull(ReflectionAccess.readMethod(ReflectionAccessTestChild(), "boom"))
    }
}
