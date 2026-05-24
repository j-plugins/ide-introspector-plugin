package com.github.xepozz.ide.introspector.core

import com.intellij.util.messages.Topic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType

/**
 * Unit tests for [TopicInspector]'s field-level scanning logic. We avoid spinning up a
 * full IntelliJ test fixture by reflecting on local classes defined inside this file —
 * the discovery logic only depends on `java.lang.reflect.Field` + `java.lang.reflect.ParameterizedType`,
 * neither of which needs the platform.
 *
 * Sample types:
 *   * [SampleListener]                    — Kotlin interface with `@JvmField val TOPIC` in the companion.
 *                                            JVM-visible as a static field on [SampleListener] itself.
 *   * [PlainCompanionListener]            — `val TOPIC` *without* `@JvmField`. Field lives on
 *                                            [PlainCompanionListener.Companion] as an instance field.
 *   * [NoTopicListener]                   — empty interface; no Topic field anywhere.
 */
class TopicInspectorReflectionTest {

    interface SampleListener {
        fun onSomething()
        companion object {
            @JvmField
            val TOPIC: Topic<SampleListener> = Topic.create("Sample", SampleListener::class.java)
        }
    }

    interface PlainCompanionListener {
        companion object {
            val TOPIC: Topic<PlainCompanionListener> =
                Topic.create("Plain", PlainCompanionListener::class.java)
        }
    }

    interface NoTopicListener

    // ====================================================================================
    // SECTION 1. @JvmField on the outer interface (recommended pattern)
    // ====================================================================================

    @Test
    fun `@JvmField TOPIC is a static field of type Topic on the outer interface`() {
        val field = findTopicField(SampleListener::class.java, static = true)
        assertNotNull("expected a static Topic field on SampleListener", field)
        assertEquals(Topic::class.java, field!!.type)
    }

    @Test
    fun `listener generic argument is preserved in the Signature attribute`() {
        val field = findTopicField(SampleListener::class.java, static = true)!!
        val listenerFqn = listenerTypeOf(field)
        assertEquals(SampleListener::class.java.name, listenerFqn)
    }

    // ====================================================================================
    // SECTION 2. Kotlin companion without @JvmField
    // ====================================================================================

    @Test
    fun `companion-declared TOPIC is discoverable via SOME Topic field on outer or companion`() {
        // Kotlin's codegen for an interface companion `val` (without @JvmField) varies between
        // versions: the field may end up as static on the outer interface, as a static on
        // `$Companion`, or as an instance field on `$Companion`. Whichever shape the compiler
        // picks, *some* `Topic`-typed Field must be visible and its generic argument must
        // resolve to the listener interface.
        val candidates = buildList {
            addAll(PlainCompanionListener::class.java.declaredFields.toList())
            PlainCompanionListener::class.java.declaredClasses
                .firstOrNull { it.simpleName == "Companion" }
                ?.declaredFields?.let { addAll(it.toList()) }
        }
        val topicFields = candidates.filter { it.type == Topic::class.java }
        assertTrue(
            "expected at least one Topic-typed field on PlainCompanionListener or its Companion",
            topicFields.isNotEmpty(),
        )
        for (f in topicFields) {
            assertEquals(PlainCompanionListener::class.java.name, listenerTypeOf(f))
        }
    }

    @Test
    fun `companion class name strips back to the outer interface for declaringClassName`() {
        // Logic mirrors TopicInspector's "if name ends with $Companion, strip it" rule.
        val companion = PlainCompanionListener::class.java.declaredClasses
            .first { it.simpleName == "Companion" }
        assertTrue(companion.name.endsWith("\$Companion"))
        val outer = companion.name.substringBeforeLast("\$Companion")
        assertEquals(PlainCompanionListener::class.java.name, outer)
    }

    // ====================================================================================
    // SECTION 3. No-topic interfaces produce no false positives
    // ====================================================================================

    @Test
    fun `interface without a Topic field yields no matches`() {
        val static = findTopicField(NoTopicListener::class.java, static = true)
        assertNull(static)
        val instance = findTopicField(NoTopicListener::class.java, static = false)
        assertNull(instance)
        assertFalse(
            "NoTopicListener has no Companion subclass either",
            NoTopicListener::class.java.declaredClasses.any { it.simpleName == "Companion" },
        )
    }

    // ====================================================================================
    // Helpers — mirror what TopicInspector does internally
    // ====================================================================================

    private fun findTopicField(cls: Class<*>, static: Boolean): Field? {
        return cls.declaredFields.firstOrNull { f ->
            f.type == Topic::class.java &&
                java.lang.reflect.Modifier.isStatic(f.modifiers) == static
        }
    }

    private fun listenerTypeOf(field: Field): String? {
        val generic = field.genericType as? ParameterizedType ?: return null
        val arg = generic.actualTypeArguments.firstOrNull() ?: return null
        return when (arg) {
            is Class<*> -> arg.name
            is ParameterizedType -> (arg.rawType as? Class<*>)?.name
            else -> null
        }
    }
}
