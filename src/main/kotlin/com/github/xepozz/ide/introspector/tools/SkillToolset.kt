package com.github.xepozz.ide.introspector.tools

import com.github.xepozz.ide.introspector.core.context.ContextServices
import com.github.xepozz.ide.introspector.model.ContextSectionResponse
import com.github.xepozz.ide.introspector.model.SkillListResponse
import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool

class SkillToolset : McpToolset {

    @McpTool(name = "skill__list")
    @McpDescription(
        """
        |Lists the curated skill cards in the bundled context corpus (id, title, tags, token cost).
        |
        |Use this when:
        |  - You want the playbook for an IntelliJ-platform task and need to discover which skills
        |    exist before fetching one with skill.get.
        |  - You want to narrow by a topic tag.
        |
        |Do NOT use this when:
        |  - You have a free-text question — use context.search to rank the whole corpus instead.
        |  - You already know the id — call skill.get directly.
        |
        |Returns: { status, skills:[{ id, title, kind, tags, tokenEstimate }], total, appliedTag }.
        |status is "ok", "unavailable" (corpus not bundled) or "error". Bodies are NOT included.
        |
        |Example:
        |  (no args)         — list every skill
        |  tag=service       — only skills tagged "service"
        """
    )
    suspend fun skill_list(
        @McpDescription("""|optional tag filter; null lists every skill""") tag: String? = null,
    ): SkillListResponse =
        runCatching { ContextServices.retriever.listSkills(tag) }
            .getOrElse { SkillListResponse(status = "error", reason = it.message) }

    @McpTool(name = "skill__get")
    @McpDescription(
        """
        |Fetches one skill body by id, clamped to a token budget with a pagination cursor.
        |
        |Use this when:
        |  - skill.list or context.search gave you an id and you want the full guidance.
        |  - You are paging a long skill — pass the returned nextOffset back as offset.
        |
        |Do NOT use this when:
        |  - You do not yet know the id — discover it via skill.list or context.search first.
        |
        |Returns: { status, id, title, kind, body, returnedTokens, truncated, nextOffset }.
        |status is "ok", "not_found", "unavailable" or "error". When truncated is true, call again
        |with offset=nextOffset to continue.
        |
        |Example:
        |  id=declaring-a-service                 — first slice, default budget
        |  id=declaring-a-service maxTokens=4000  — a larger slice
        """
    )
    suspend fun skill_get(
        @McpDescription("""|stable skill id from skill.list or context.search""") id: String,
        @McpDescription("""|token budget for the returned body; capped per call""") maxTokens: Int = 2000,
        @McpDescription("""|character offset to resume from; pass a prior nextOffset""") offset: Int = 0,
    ): ContextSectionResponse =
        runCatching { ContextServices.retriever.section(id, maxTokens, offset) }
            .getOrElse { ContextSectionResponse(status = "error", id = id, reason = it.message) }
}
