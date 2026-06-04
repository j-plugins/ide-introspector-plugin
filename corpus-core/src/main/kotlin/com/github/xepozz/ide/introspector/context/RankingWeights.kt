package com.github.xepozz.ide.introspector.context

data class RankingWeights(
    val bodyBm25: Double = 1.0,
    val titleMatch: Double = 0.6,
    val tagMatch: Double = 0.8,
    val manualPriority: Double = 0.3,
    val graphProximity: Double = 0.5,
    val scoreFloor: Double = 0.05,
) {
    companion object {
        val DEFAULT = RankingWeights()
    }
}
