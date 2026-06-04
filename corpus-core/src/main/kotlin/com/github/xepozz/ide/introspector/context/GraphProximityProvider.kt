package com.github.xepozz.ide.introspector.context

fun interface GraphProximityProvider {
    fun proximity(entry: ManifestEntry): Double

    companion object {
        val NONE = GraphProximityProvider { 0.0 }
    }
}
