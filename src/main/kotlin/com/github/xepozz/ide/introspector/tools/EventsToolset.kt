package com.github.xepozz.ide.introspector.tools

import com.github.xepozz.ide.introspector.core.PluginInventory
import com.github.xepozz.ide.introspector.model.ListListenersResponse
import com.github.xepozz.ide.introspector.model.ListTopicsResponse
import com.github.xepozz.ide.introspector.util.areaMatches
import com.github.xepozz.ide.introspector.util.containsQuery
import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool

class EventsToolset : McpToolset {

    @McpTool(name = "events.list_listeners")
    @McpDescription(
        """
        |Lists IntelliJ message-bus listeners declared statically in plugin.xml via
        |`<applicationListeners>` and `<projectListeners>`. These are pairs of (topic class,
        |listener class) wired up by the platform on application/project initialization —
        |IntelliJ's idiomatic, declarative event-subscription mechanism.
        |
        |Use this when: you want to find "who reacts to file edits / VCS state / project open?",
        |"which listeners does plugin X register?", "what are the application-level listeners
        |that fire on startup?". Common starting point for understanding plugin event flow.
        |
        |Do NOT use this when: you want to know who subscribed *at runtime* via
        |`messageBus.connect().subscribe(...)` (out of scope — those subscriptions are
        |imperative, not enumerable), or you want the available topic classes themselves (look
        |at the topicClass field on the returned listeners, or grep platform sources for
        |`Topic<...>`).
        |
        |Returns: { listeners: ListenerInfo[], total: int } where each ListenerInfo has
        |topicClass (FQCN of the Topic), listenerClass (FQCN of the implementation),
        |area ('application'|'project'), activeInTestMode, activeInHeadlessMode, os (when
        |restricted), providedByPluginId/Name.
        |
        |Examples:
        |  topicContains="FileEditorManager"                              — every listener for FileEditorManager.Listener topics
        |  area="project", providedByPlugin="com.github.xepozz.ide.introspector" — this plugin's project listeners
        |  listenerContains="StartupActivity"                             — listeners whose impl mentions StartupActivity
        """
    )
    suspend fun events_list_listeners(
        @McpDescription("'application', 'project', or 'all'. Default 'all'.")
        area: String = "all",
        @McpDescription("Restrict to listeners contributed by this plugin id.")
        providedByPlugin: String? = null,
        @McpDescription("Case-insensitive substring filter on the topic FQCN.")
        topicContains: String? = null,
        @McpDescription("Case-insensitive substring filter on the listener implementation FQCN.")
        listenerContains: String? = null,
        @McpDescription("Cap on returned listeners. Default 500.")
        limit: Int = 500,
    ): ListListenersResponse {
        val filtered = PluginInventory.getInstance().listeners()
            .filter { areaMatches(area, it.area) }
            .filter { providedByPlugin == null || it.providedByPluginId == providedByPlugin }
            .filter { it.topicClass.containsQuery(topicContains) }
            .filter { it.listenerClass.containsQuery(listenerContains) }
        return ListListenersResponse(filtered.take(limit), filtered.size)
    }

    @McpTool(name = "events.find_listeners_of_topic")
    @McpDescription(
        """
        |Reverse-lookup: given a Topic FQCN, lists every static listener registered against it.
        |
        |Use this when: you have a specific topic class (e.g. `com.intellij.openapi.vfs.newvfs.BulkFileListener`)
        |and want to know what reacts to its events.
        |
        |Do NOT use this when: you have a substring (use events.list_listeners with topicContains).
        |
        |Returns: { listeners: ListenerInfo[], total: int }.
        |
        |Examples:
        |  topicClass="com.intellij.openapi.vfs.newvfs.BulkFileListener"
        |  topicClass="com.intellij.openapi.fileEditor.FileEditorManagerListener"
        """
    )
    suspend fun events_find_listeners_of_topic(
        @McpDescription("Fully-qualified Topic class name. Use the topicClass values from events.list_listeners.")
        topicClass: String,
    ): ListListenersResponse {
        val matches = PluginInventory.getInstance().listeners()
            .filter { it.topicClass == topicClass }
        return ListListenersResponse(matches, matches.size)
    }

    @McpTool(name = "events.list_topics")
    @McpDescription(
        """
        |Lists message-bus `Topic<L>` declarations contributed by plugins. A Topic is a
        |channel — typically a `@JvmField val TOPIC: Topic<MyListener>` in the listener
        |interface's companion (the recommended IDE convention) — that other code publishes
        |events to via `messageBus.syncPublisher(TOPIC).onEvent(...)`.
        |
        |Discovery is bytecode-only: each plugin's jars are walked and `Topic`-typed static
        |fields (or instance fields on Kotlin `${'$'}Companion`) are collected. We do NOT
        |initialise plugin classes, so the Topic's `displayName` / `broadcastDirection`
        |are not in the output — only declaring class, field name, and the listener generic
        |type `L`.
        |
        |Use this when: "what events can plugin X broadcast?", "what topics does the IDE
        |define for VFS / editor / project events?", "is there already a topic for state Y
        |so I can subscribe instead of polling?".
        |
        |Do NOT use this when: you want subscribers (use events.list_listeners /
        |events.find_listeners_of_topic), or you need the Topic instance's runtime
        |attributes — those aren't read without triggering `<clinit>`.
        |
        |Scope: scanning is lazy and expensive (it walks each plugin's classpath). By default
        |we scan only **non-bundled** plugins (typically a handful), since bundled IDE plugins
        |contain tens of thousands of classes and freeze the call. Set includeBundled=true to
        |include them — or, much faster, pass providedByPlugin to scan a single specific plugin.
        |
        |Returns: { topics: TopicInfo[], total: int } where each TopicInfo has id
        |(declaringClass + "." + fieldName), declaringClassName, fieldName,
        |listenerClassName, onCompanion (true when held by `${'$'}Companion`),
        |providedByPluginId/Name.
        |
        |Examples:
        |  providedByPlugin="com.github.xepozz.ide.introspector"          — single plugin (fast)
        |  providedByPlugin="com.intellij", listenerContains="VirtualFile" — bundled plugin's VFS topics
        |  includeBundled=true, listenerContains="FileEditorManager"      — broad scan (slow)
        """
    )
    suspend fun events_list_topics(
        @McpDescription("Restrict to topics contributed by this plugin id. When set, only this one plugin's classpath is scanned (fast).")
        providedByPlugin: String? = null,
        @McpDescription("Case-insensitive substring filter on the declaring class FQCN.")
        declaringClassContains: String? = null,
        @McpDescription("Case-insensitive substring filter on the listener interface FQCN.")
        listenerContains: String? = null,
        @McpDescription("Include bundled (IDE-shipped) plugins in the scan. Default false — bundled scan is slow.")
        includeBundled: Boolean = false,
        @McpDescription("Cap on returned topics. Default 500.")
        limit: Int = 500,
    ): ListTopicsResponse {
        val inv = PluginInventory.getInstance()
        val targetIds = if (providedByPlugin != null) {
            listOf(providedByPlugin)
        } else {
            inv.plugins().filter { includeBundled || !it.isBundled }.map { it.id }
        }
        inv.scanTopicsForPlugins(targetIds)
        val targetIdsSet = targetIds.toSet()

        val filtered = inv.topics()
            .filter { it.providedByPluginId in targetIdsSet }
            .filter { it.declaringClassName.containsQuery(declaringClassContains) }
            .filter { it.listenerClassName.containsQuery(listenerContains) }
        return ListTopicsResponse(filtered.take(limit), filtered.size)
    }
}
