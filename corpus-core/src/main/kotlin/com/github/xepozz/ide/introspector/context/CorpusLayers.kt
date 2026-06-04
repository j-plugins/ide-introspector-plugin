package com.github.xepozz.ide.introspector.context

object CorpusLayers {
    private const val MANUAL_PREFIX = "manual/"
    private const val SDK_PLATFORM_PREFIX = "generated/sdk-platform/"
    private const val INTROSPECTOR_DOCS_PREFIX = "generated/introspector-docs/"
    private const val EXAMPLES_PREFIX = "generated/examples/"

    fun fromRelativePath(relativePath: String): CorpusLayer {
        val normalized = normalize(relativePath)
        return when {
            normalized.startsWith(MANUAL_PREFIX) -> CorpusLayer.MANUAL
            normalized.startsWith(SDK_PLATFORM_PREFIX) -> CorpusLayer.SDK_PLATFORM
            normalized.startsWith(INTROSPECTOR_DOCS_PREFIX) -> CorpusLayer.INTROSPECTOR_DOCS
            normalized.startsWith(EXAMPLES_PREFIX) -> CorpusLayer.EXAMPLES
            else -> CorpusLayer.UNKNOWN
        }
    }

    fun sourceFor(layer: CorpusLayer): Source =
        when (layer) {
            CorpusLayer.MANUAL -> Source.MANUAL
            else -> Source.GENERATED
        }

    private fun normalize(relativePath: String): String =
        relativePath
            .replace('\\', '/')
            .removePrefix("./")
            .removePrefix("/")
}
