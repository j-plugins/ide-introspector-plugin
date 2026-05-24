package com.github.xepozz.ide.introspector.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * Pure-JVM coverage for the small helper functions on [ClassCatalog] — no IntelliJ
 * platform fixture required. Covers:
 *  - `matchesPackagePrefix` truth table (prefix-as-string trap: `com.acme` vs `com.acmex`).
 *  - `clampLimit` validation boundaries.
 *
 * Platform-level coverage (filtering against real PSI) lives in
 * [com.github.xepozz.ide.introspector.core.platform.ClassCatalogPlatformTest].
 */
class ClassCatalogFilterTest {

    // ============================================================================
    // matchesPackagePrefix
    // ============================================================================

    @Test
    fun nullPrefixMatchesEverything() {
        assertTrue(ClassCatalog.matchesPackagePrefix("com.acme.Foo", null))
        assertTrue(ClassCatalog.matchesPackagePrefix("", null))
    }

    @Test
    fun emptyPrefixMatchesEverything() {
        assertTrue(ClassCatalog.matchesPackagePrefix("com.acme.Foo", ""))
        assertTrue(ClassCatalog.matchesPackagePrefix("", ""))
    }

    @Test
    fun exactPrefixMatches() {
        assertTrue(ClassCatalog.matchesPackagePrefix("com.acme", "com.acme"))
    }

    @Test
    fun subPackageOfPrefixMatches() {
        assertTrue(ClassCatalog.matchesPackagePrefix("com.acme.sub", "com.acme"))
        assertTrue(ClassCatalog.matchesPackagePrefix("com.acme.sub.deeper", "com.acme"))
    }

    @Test
    fun siblingPackageWithSamePrefixDoesNotMatch() {
        // Regression for the string-prefix-as-package-prefix trap. `com.acmex` shares
        // a string prefix with `com.acme` but is a different package — must not match.
        assertFalse(ClassCatalog.matchesPackagePrefix("com.acmex", "com.acme"))
        assertFalse(ClassCatalog.matchesPackagePrefix("com.acmex.Foo", "com.acme"))
    }

    @Test
    fun nonMatchingPackagesAreRejected() {
        assertFalse(ClassCatalog.matchesPackagePrefix("org.example.Foo", "com.acme"))
        assertFalse(ClassCatalog.matchesPackagePrefix("", "com.acme"))
    }

    // ============================================================================
    // clampLimit
    // ============================================================================

    @Test
    fun clampLimitAcceptsValidRange() {
        assertEquals(1, ClassCatalog.clampLimit(1))
        assertEquals(500, ClassCatalog.clampLimit(500))
        assertEquals(ClassCatalog.MAX_LIMIT, ClassCatalog.clampLimit(ClassCatalog.MAX_LIMIT))
    }

    @Test
    fun clampLimitRejectsZero() {
        val ex = assertThrowsIAE { ClassCatalog.clampLimit(0) }
        assertTrue(
            "message must mention valid range; got '${ex.message}'",
            ex.message?.contains("limit must be in 1..${ClassCatalog.MAX_LIMIT}") == true,
        )
    }

    @Test
    fun clampLimitRejectsNegative() {
        assertThrowsIAE { ClassCatalog.clampLimit(-1) }
        assertThrowsIAE { ClassCatalog.clampLimit(-1000) }
    }

    @Test
    fun clampLimitRejectsOverMax() {
        assertThrowsIAE { ClassCatalog.clampLimit(ClassCatalog.MAX_LIMIT + 1) }
    }

    private inline fun assertThrowsIAE(block: () -> Unit): IllegalArgumentException {
        try {
            block()
        } catch (e: IllegalArgumentException) {
            return e
        }
        fail("expected IllegalArgumentException")
        error("unreachable")
    }
}
