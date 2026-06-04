package com.github.xepozz.ide.introspector.core.context

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContextRetrieverTest {

    private val retriever = ContextRetriever()

    @Test
    fun listsSeededSkillsFromBundledCorpus() {
        val response = retriever.listSkills(null)
        assertEquals("ok", response.status)
        assertTrue(response.skills.any { it.id == "declaring-a-service" })
        assertTrue(response.skills.any { it.id == "registering-an-extension-point" })
    }

    @Test
    fun filtersSkillsByTag() {
        val response = retriever.listSkills("service")
        assertEquals("ok", response.status)
        assertTrue(response.skills.all { "service" in it.tags })
        assertTrue(response.skills.any { it.id == "declaring-a-service" })
    }

    @Test
    fun searchRanksExtensionPointSkillFirst() {
        val response = retriever.search("register an extension point", 5)
        assertEquals("ok", response.status)
        assertTrue(response.hits.isNotEmpty())
        assertEquals("registering-an-extension-point", response.hits.first().id)
    }

    @Test
    fun sectionReturnsBodyForKnownId() {
        val response = retriever.section("declaring-a-service", 2000, 0)
        assertEquals("ok", response.status)
        assertEquals("declaring-a-service", response.id)
        assertTrue(response.body!!.contains("service"))
    }

    @Test
    fun sectionReturnsNotFoundForUnknownId() {
        val response = retriever.section("does-not-exist", 2000, 0)
        assertEquals("not_found", response.status)
    }
}
