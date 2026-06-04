package com.github.xepozz.ide.introspector.tools

import com.github.xepozz.ide.introspector.core.context.ContextServices
import com.github.xepozz.ide.introspector.model.ContextSearchResponse
import com.github.xepozz.ide.introspector.model.ContextSectionResponse
import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool

class ContextToolset : McpToolset {

    @McpTool(name = "context__search")
    @McpDescription(
        """
        |Ranks the whole context corpus (curated skills + generated reference) against a query and
        |returns locators only, for progressive disclosure.
        |
        |Use this when:
        |  - You have a free-text task or keyword and want the most relevant context entries before
        |    pulling any bodies.
        |  - You want to discover ids to feed into context.get / skill.get.
        |
        |Do NOT use this when:
        |  - You only want curated skills by tag — skill.list is cheaper.
        |  - You already know the id — call context.get directly.
        |
        |Returns: { status, query, hits:[{ id, title, kind, source, score, tokenEstimate,
        |matchedTerms }], total }. status is "ok", "unavailable" or "error". Bodies are NOT included;
        |fetch one with context.get.
        |
        |Example:
        |  query="register an extension point"   — ranked locators
        |  query="service lifecycle" limit=5     — top five
        """
    )
    suspend fun context_search(
        @McpDescription("""|natural-language task or keywords""") query: String,
        @McpDescription("""|maximum number of hits to return""") limit: Int = 10,
    ): ContextSearchResponse =
        runCatching { ContextServices.retriever.search(query, limit) }
            .getOrElse { ContextSearchResponse(status = "error", query = query, reason = it.message) }

    @McpTool(name = "context__get")
    @McpDescription(
        """
        |Fetches one corpus entry body by id, clamped to a token budget with a pagination cursor.
        |
        |Use this when:
        |  - context.search gave you an id and you want the full text of that entry.
        |  - You are paging a long entry — pass the returned nextOffset back as offset.
        |
        |Do NOT use this when:
        |  - You do not yet know the id — rank candidates with context.search first.
        |
        |Returns: { status, id, title, kind, body, returnedTokens, truncated, nextOffset }.
        |status is "ok", "not_found", "unavailable" or "error". When truncated is true, call again
        |with offset=nextOffset to continue.
        |
        |Example:
        |  id=the-extension-point-model                — first slice
        |  id=the-extension-point-model offset=2000    — continue paging
        """
    )
    suspend fun context_get(
        @McpDescription("""|stable entry id from context.search""") id: String,
        @McpDescription("""|token budget for the returned body; capped per call""") maxTokens: Int = 2000,
        @McpDescription("""|character offset to resume from; pass a prior nextOffset""") offset: Int = 0,
    ): ContextSectionResponse =
        runCatching { ContextServices.retriever.section(id, maxTokens, offset) }
            .getOrElse { ContextSectionResponse(status = "error", id = id, reason = it.message) }
}
