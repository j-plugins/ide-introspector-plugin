package com.github.xepozz.ide.introspector.context

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenEstimatorTest {
    @Test
    fun returnsZeroForEmptyText() {
        assertEquals(0, TokenEstimator.estimate(""))
    }

    @Test
    fun returnsZeroForBlankText() {
        assertEquals(0, TokenEstimator.estimate("   \n\t "))
    }

    @Test
    fun returnsAtLeastOneForShortNonBlankText() {
        assertTrue(TokenEstimator.estimate("a") >= 1)
    }

    @Test
    fun roundsUpShortStringToOne() {
        assertEquals(1, TokenEstimator.estimate("abc"))
    }

    @Test
    fun computesCharsOverFourForExactMultiple() {
        assertEquals(2, TokenEstimator.estimate("12345678"))
    }

    @Test
    fun roundsUpNonMultipleLength() {
        assertEquals(3, TokenEstimator.estimate("123456789"))
    }
}
