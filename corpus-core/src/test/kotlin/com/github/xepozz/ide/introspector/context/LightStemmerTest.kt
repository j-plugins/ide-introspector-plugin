package com.github.xepozz.ide.introspector.context

import org.junit.Assert.assertEquals
import org.junit.Test

class LightStemmerTest {
    @Test
    fun stripsPluralSToSingular() {
        assertEquals("inspection", LightStemmer.stem("inspections"))
    }

    @Test
    fun keepsWordEndingInDoubleS() {
        assertEquals("class", LightStemmer.stem("class"))
    }

    @Test
    fun stripsTrailingSEvenWhenResultLooksOdd() {
        assertEquals("statu", LightStemmer.stem("status"))
    }

    @Test
    fun stripsSimplePlural() {
        assertEquals("tool", LightStemmer.stem("tools"))
    }

    @Test
    fun stripsIngFromLongEnoughWord() {
        assertEquals("runn", LightStemmer.stem("running"))
    }

    @Test
    fun stripsEdFromLongEnoughWord() {
        assertEquals("inspect", LightStemmer.stem("inspected"))
    }

    @Test
    fun convertsIesToY() {
        assertEquals("propery", LightStemmer.stem("properies"))
    }

    @Test
    fun keepsShortPluralWhenStemWouldBeTooShort() {
        assertEquals("ids", LightStemmer.stem("ids"))
    }

    @Test
    fun keepsTwoLetterToken() {
        assertEquals("id", LightStemmer.stem("id"))
    }

    @Test
    fun doesNotStripIngFromShortWord() {
        assertEquals("zing", LightStemmer.stem("zing"))
    }
}
