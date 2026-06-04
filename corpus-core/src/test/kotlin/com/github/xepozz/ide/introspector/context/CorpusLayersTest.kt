package com.github.xepozz.ide.introspector.context

import org.junit.Assert.*
import org.junit.Test

class CorpusLayersTest {
    @Test
    fun manual_prefix_maps_to_manual_layer() {
        assertEquals(CorpusLayer.MANUAL, CorpusLayers.fromRelativePath("manual/skills/foo.md"))
    }

    @Test
    fun sdk_platform_prefix_maps_to_sdk_platform_layer() {
        assertEquals(
            CorpusLayer.SDK_PLATFORM,
            CorpusLayers.fromRelativePath("generated/sdk-platform/foo.md"),
        )
    }

    @Test
    fun introspector_docs_prefix_maps_to_introspector_docs_layer() {
        assertEquals(
            CorpusLayer.INTROSPECTOR_DOCS,
            CorpusLayers.fromRelativePath("generated/introspector-docs/foo.md"),
        )
    }

    @Test
    fun examples_prefix_maps_to_examples_layer() {
        assertEquals(
            CorpusLayer.EXAMPLES,
            CorpusLayers.fromRelativePath("generated/examples/plugins/x/y.md"),
        )
    }

    @Test
    fun unrecognized_path_maps_to_unknown_layer() {
        assertEquals(CorpusLayer.UNKNOWN, CorpusLayers.fromRelativePath("random/place/foo.md"))
    }

    @Test
    fun backslash_separators_are_normalized() {
        assertEquals(CorpusLayer.MANUAL, CorpusLayers.fromRelativePath("manual\\skills\\foo.md"))
    }

    @Test
    fun leading_dot_slash_is_stripped() {
        assertEquals(CorpusLayer.MANUAL, CorpusLayers.fromRelativePath("./manual/foo.md"))
    }

    @Test
    fun leading_slash_is_stripped() {
        assertEquals(
            CorpusLayer.SDK_PLATFORM,
            CorpusLayers.fromRelativePath("/generated/sdk-platform/foo.md"),
        )
    }

    @Test
    fun source_for_manual_layer_is_manual() {
        assertEquals(Source.MANUAL, CorpusLayers.sourceFor(CorpusLayer.MANUAL))
    }

    @Test
    fun source_for_sdk_platform_layer_is_generated() {
        assertEquals(Source.GENERATED, CorpusLayers.sourceFor(CorpusLayer.SDK_PLATFORM))
    }

    @Test
    fun source_for_introspector_docs_layer_is_generated() {
        assertEquals(Source.GENERATED, CorpusLayers.sourceFor(CorpusLayer.INTROSPECTOR_DOCS))
    }

    @Test
    fun source_for_examples_layer_is_generated() {
        assertEquals(Source.GENERATED, CorpusLayers.sourceFor(CorpusLayer.EXAMPLES))
    }

    @Test
    fun source_for_unknown_layer_is_generated() {
        assertEquals(Source.GENERATED, CorpusLayers.sourceFor(CorpusLayer.UNKNOWN))
    }
}
