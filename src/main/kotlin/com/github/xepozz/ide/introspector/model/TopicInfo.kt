package com.github.xepozz.ide.introspector.model

import kotlinx.serialization.Serializable

/**
 * One IntelliJ message-bus [com.intellij.util.messages.Topic] declared by a plugin —
 * typically as a static `@JvmField val TOPIC: Topic<MyListener>` in the listener
 * interface's companion (the recommended IDE convention), or as an instance field
 * on a Kotlin `Companion` singleton.
 *
 * Discovery is bytecode-only: we never call the static initialiser of arbitrary plugin
 * classes, so [broadcastDirection] / [displayName] (which require the Topic instance) are
 * left null. Field-level information (type, generic parameter) is read from the class's
 * `Signature` attribute via reflection without triggering `<clinit>`.
 */
@Serializable
data class TopicInfo(
    /** Unique identifier: declaringClassName + "." + fieldName. */
    val id: String,
    /** Fully-qualified class that owns the field (the outer class, not its `$Companion`). */
    val declaringClassName: String,
    /** Name of the field that holds the Topic value. */
    val fieldName: String,
    /** Fully-qualified class of the listener interface (the L in `Topic<L>`). */
    val listenerClassName: String,
    /** True when the field lives on a Kotlin `Companion` instance (no `@JvmField`). */
    val onCompanion: Boolean,
    val providedByPluginId: String,
    val providedByPluginName: String?,
)

@Serializable
data class ListTopicsResponse(
    val topics: List<TopicInfo>,
    val total: Int,
)
