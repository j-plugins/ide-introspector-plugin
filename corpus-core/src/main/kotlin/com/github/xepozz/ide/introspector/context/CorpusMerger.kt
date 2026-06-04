package com.github.xepozz.ide.introspector.context

object CorpusMerger {
    fun merge(entries: List<CorpusEntry>): MergeResult {
        val winners = mutableListOf<CorpusEntry>()
        val issues = mutableListOf<ValidationIssue>()
        val groups = entries.groupBy { it.id }
        for ((id, group) in groups) {
            resolveGroup(id, group, winners, issues)
        }
        return MergeResult(
            entries = winners.sortedBy { it.id },
            issues = issues,
        )
    }

    private fun resolveGroup(
        id: String,
        group: List<CorpusEntry>,
        winners: MutableList<CorpusEntry>,
        issues: MutableList<ValidationIssue>,
    ) {
        val winningRank = group.minOf { rankOf(it.source) }
        val winningTier = group
            .filter { rankOf(it.source) == winningRank }
            .sortedBy { it.relativePath }
        val winner = winningTier.first()
        winners += winner
        if (winningTier.size == 1) {
            return
        }
        for (loser in winningTier.drop(1)) {
            issues += ValidationIssue(
                severity = Severity.ERROR,
                code = IssueCode.DUPLICATE_ID_IN_TIER,
                message = "Duplicate id '$id' in tier; winner is ${winner.relativePath}",
                sourcePath = loser.relativePath,
            )
        }
    }

    private fun rankOf(source: Source): Int =
        when (source) {
            Source.MANUAL -> 0
            Source.GENERATED -> 1
        }
}
