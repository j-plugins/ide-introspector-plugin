package com.github.xepozz.ide.introspector.model

import kotlinx.serialization.Serializable

/**
 * One MessageBus listener registration harvested from a plugin.xml declaration
 * (either `<applicationListeners>` or `<projectListeners>`).
 *
 * Listeners registered programmatically via `MessageBus.connect().subscribe(...)`
 * are NOT included — see the `arch.list_listeners` tool description for details.
 */
@Serializable
data class ListenerInfo(
    /** Fully-qualified class name of the topic interface, e.g. `com.intellij.openapi.fileEditor.FileEditorManagerListener`. */
    val topicClass: String,
    /** Fully-qualified class name of the plugin's listener implementation. */
    val listenerClass: String,
    /** "application" | "project". */
    val scope: String,
    /** Plugin id that declared the listener, e.g. `com.intellij` / `org.jetbrains.kotlin`. */
    val providedByPluginId: String,
    /** Human-readable plugin display name (may be null in fork/test stubs). */
    val providedByPluginName: String?,
    /** XML attribute `activeInTestMode` (defaults to `true` when absent). */
    val activeInTestMode: Boolean,
    /** XML attribute `activeInHeadlessMode` (defaults to `true` when absent). */
    val activeInHeadlessMode: Boolean,
    /** Optional `os` attribute from the XML, present only on platform-specific declarations. */
    val os: String? = null,
)

@Serializable
data class ListListenersResponse(
    val listeners: List<ListenerInfo>,
    val total: Int,
)
