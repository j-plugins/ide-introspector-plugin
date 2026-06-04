package com.github.xepozz.ide.introspector.core.context

import com.github.xepozz.ide.introspector.context.Bm25Ranker
import com.github.xepozz.ide.introspector.context.Manifest
import com.github.xepozz.ide.introspector.context.TokenBudgeter
import com.github.xepozz.ide.introspector.model.ContextSearchResponse
import com.github.xepozz.ide.introspector.model.ContextSectionResponse
import com.github.xepozz.ide.introspector.model.SearchHit
import com.github.xepozz.ide.introspector.model.SkillListResponse
import com.github.xepozz.ide.introspector.model.SkillRef

class ContextRetriever(
    private val resolver: CorpusResolver = CorpusResolver(),
    private val ranker: Bm25Ranker = Bm25Ranker(),
    private val budgeter: TokenBudgeter = TokenBudgeter(),
) {
    fun listSkills(tag: String?): SkillListResponse {
        val manifest = readyManifest() ?: return SkillListResponse(status = STATUS_UNAVAILABLE, reason = unavailableReason())
        val skills = manifest.entries
            .filter { it.kind == KIND_SKILL }
            .filter { tag == null || tag in it.tags }
            .map { SkillRef(it.id, it.title, it.kind, it.tags, it.tokenEstimate) }
            .sortedBy { it.id }
        return SkillListResponse(status = STATUS_OK, skills = skills, total = skills.size, appliedTag = tag)
    }

    fun search(query: String, limit: Int): ContextSearchResponse {
        val manifest = readyManifest()
            ?: return ContextSearchResponse(status = STATUS_UNAVAILABLE, query = query, reason = unavailableReason())
        val hits = ranker.rank(manifest, query, limit).map {
            SearchHit(it.entry.id, it.entry.title, it.entry.kind, it.entry.source, it.score, it.entry.tokenEstimate, it.matchedTerms)
        }
        return ContextSearchResponse(status = STATUS_OK, query = query, hits = hits, total = hits.size)
    }

    fun section(id: String, maxTokens: Int, offset: Int): ContextSectionResponse {
        val manifest = readyManifest()
            ?: return ContextSectionResponse(status = STATUS_UNAVAILABLE, id = id, reason = unavailableReason())
        val entry = manifest.entries.find { it.id == id }
            ?: return ContextSectionResponse(status = STATUS_NOT_FOUND, id = id, reason = "no corpus entry with id '$id'")
        val body = resolver.body(entry.relativePath)
            ?: return ContextSectionResponse(status = STATUS_UNAVAILABLE, id = id, reason = "corpus body missing for id '$id'")
        val section = budgeter.clamp(body, maxTokens, offset)
        return ContextSectionResponse(
            status = STATUS_OK,
            id = entry.id,
            title = entry.title,
            kind = entry.kind,
            body = section.text,
            returnedTokens = section.returnedTokens,
            truncated = section.truncated,
            nextOffset = section.nextOffset,
        )
    }

    private fun readyManifest(): Manifest? = (resolver.state() as? CorpusState.Ready)?.manifest

    private fun unavailableReason(): String =
        (resolver.state() as? CorpusState.Unavailable)?.reason ?: "context corpus is unavailable"

    private companion object {
        const val STATUS_OK = "ok"
        const val STATUS_UNAVAILABLE = "unavailable"
        const val STATUS_NOT_FOUND = "not_found"
        const val KIND_SKILL = "skill"
    }
}
