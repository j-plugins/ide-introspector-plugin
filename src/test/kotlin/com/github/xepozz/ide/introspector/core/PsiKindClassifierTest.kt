package com.github.xepozz.ide.introspector.core

import com.intellij.psi.PsiElement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

/**
 * Pure-JVM unit tests for [PsiKindClassifier]. Real Java/Kotlin PSI instances require the
 * platform and live under
 * [com.github.xepozz.ide.introspector.core.platform.PsiOutlineCollectorPlatformTest] —
 * here we exercise the no-platform paths:
 *
 *   1. Unknown PsiElement → `kind="unknown"` with the PsiNamedElement-derived name.
 *   2. Kotlin reflective dispatch ignores non-`Kt*` class names (no NoClassDefFoundError).
 *   3. `kindOf` shortcut returns the same `kind` field as the full `classify` call.
 */
class PsiKindClassifierTest {

    /** Build a minimal PsiElement proxy that returns null for every method. */
    private fun proxyPsiElement(): PsiElement = Proxy.newProxyInstance(
        PsiElement::class.java.classLoader,
        arrayOf(PsiElement::class.java),
    ) { _, method, _ ->
        when (method.returnType) {
            java.lang.Boolean.TYPE -> false
            java.lang.Integer.TYPE -> 0
            java.lang.Long.TYPE -> 0L
            else -> null
        }
    } as PsiElement

    @Test
    fun unknownElementClassifiesAsUnknown() {
        val element = proxyPsiElement()
        val classified = PsiKindClassifier.classify(element)
        assertEquals("unknown", classified.kind)
        assertNull("unknown elements have no FQN", classified.fqn)
        assertTrue("unknown elements have no modifiers", classified.modifiers.isEmpty())
        assertNull(classified.returnType)
        assertNull(classified.typeText)
    }

    @Test
    fun kindOfShortcutMatchesClassify() {
        val element = proxyPsiElement()
        assertEquals(
            "kindOf must return the same value as classify(...).kind",
            PsiKindClassifier.classify(element).kind,
            PsiKindClassifier.kindOf(element),
        )
    }

    @Test
    fun nonKtClassNameDoesNotTriggerKotlinPath() {
        // The proxy's javaClass.simpleName starts with "$Proxy", not "Kt", so the Kotlin
        // dispatch must skip cleanly and the result falls into the unknown bucket.
        val element = proxyPsiElement()
        val simpleName = element.javaClass.simpleName
        assertTrue(
            "test proxy class name should not start with 'Kt'; got '$simpleName'",
            !simpleName.startsWith("Kt"),
        )
        val classified = PsiKindClassifier.classify(element)
        assertEquals("unknown", classified.kind)
    }
}
