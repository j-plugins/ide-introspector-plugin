package com.github.xepozz.ide.introspector.core.context

import com.github.xepozz.ide.introspector.context.Manifest
import com.github.xepozz.ide.introspector.core.internal.TtlCache
import kotlinx.serialization.json.Json

sealed interface CorpusState {
    data class Ready(val manifest: Manifest) : CorpusState
    data class Unavailable(val reason: String) : CorpusState
}

class CorpusResolver(
    private val resourceRoot: String = "/context-corpus",
    ttlMs: Long = 60_000L,
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val cache = TtlCache(ttlMs) { loadState() }

    fun state(forceRefresh: Boolean = false): CorpusState = cache.get(forceRefresh)

    fun body(relativePath: String): String? = readResource("$resourceRoot/$relativePath")

    private fun loadState(): CorpusState {
        val text = readResource("$resourceRoot/manifest.json")
            ?: return CorpusState.Unavailable("context corpus is not bundled")
        return runCatching { CorpusState.Ready(json.decodeFromString(Manifest.serializer(), text)) }
            .getOrElse { CorpusState.Unavailable("context corpus manifest is unreadable: ${it.message}") }
    }

    private fun readResource(path: String): String? =
        javaClass.getResourceAsStream(path)?.use { it.readBytes().toString(Charsets.UTF_8) }
}
