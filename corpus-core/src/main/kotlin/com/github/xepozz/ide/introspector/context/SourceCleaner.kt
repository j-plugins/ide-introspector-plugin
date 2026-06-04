package com.github.xepozz.ide.introspector.context

class SourceCleaner {
    fun clean(source: String): String {
        val lines = source.split("\n")
        val withoutLeadingHeader = stripLeadingHeader(lines)
        val withoutDecompilerArtifacts = withoutLeadingHeader.filterNot { isDecompilerArtifactLine(it) }
        val withoutSyntheticAccess = withoutDecompilerArtifacts.filterNot { isSyntheticAccessLine(it) }
        val withoutImports = removeImportRuns(withoutSyntheticAccess)
        return normalizeWhitespace(withoutImports)
    }

    private fun stripLeadingHeader(lines: List<String>): List<String> {
        var index = 0
        while (index < lines.size && lines[index].isBlank()) {
            index++
        }
        if (index >= lines.size) {
            return lines
        }
        val first = lines[index].trimStart()
        return when {
            first.startsWith("//") -> stripLeadingLineComments(lines, index)
            first.startsWith("/*") -> stripLeadingBlockComment(lines, index)
            else -> lines
        }
    }

    private fun stripLeadingLineComments(lines: List<String>, start: Int): List<String> {
        var index = start
        while (index < lines.size && lines[index].trimStart().startsWith("//")) {
            index++
        }
        return dropRangeAndLeadingBlanks(lines, start, index)
    }

    private fun stripLeadingBlockComment(lines: List<String>, start: Int): List<String> {
        val firstTrimmed = lines[start].trimStart()
        if (firstTrimmed.contains("*/")) {
            val afterClose = firstTrimmed.substringAfter("*/").trim()
            if (afterClose.isNotEmpty()) {
                return lines
            }
            return dropRangeAndLeadingBlanks(lines, start, start + 1)
        }
        var index = start + 1
        while (index < lines.size && !lines[index].contains("*/")) {
            index++
        }
        if (index >= lines.size) {
            return lines
        }
        return dropRangeAndLeadingBlanks(lines, start, index + 1)
    }

    private fun dropRangeAndLeadingBlanks(lines: List<String>, start: Int, endExclusive: Int): List<String> {
        val before = lines.subList(0, start)
        var rest = endExclusive
        while (rest < lines.size && lines[rest].isBlank()) {
            rest++
        }
        return before + lines.subList(rest, lines.size)
    }

    private fun isDecompilerArtifactLine(line: String): Boolean {
        if (line.contains(DECOMPILER_FIELD_MARKER)) {
            return true
        }
        return DECOMPILER_TAIL_COMMENT.matches(line.trim())
    }

    private fun isSyntheticAccessLine(line: String): Boolean {
        if (isInsideStringContext(line)) {
            return false
        }
        return SYNTHETIC_ACCESS.containsMatchIn(line)
    }

    private fun isInsideStringContext(line: String): Boolean {
        val match = SYNTHETIC_ACCESS.find(line) ?: return false
        val before = line.substring(0, match.range.first)
        return before.count { it == '"' } % 2 == 1
    }

    private fun removeImportRuns(lines: List<String>): List<String> =
        lines.filterNot { IMPORT_LINE.matches(it) }

    private fun normalizeWhitespace(lines: List<String>): String {
        val trimmedTrailing = lines.map { TRAILING_SPACES.replace(it, "") }
        val collapsed = collapseBlankRuns(trimmedTrailing)
        val withoutEdgeBlanks = collapsed
            .dropWhile { it.isBlank() }
            .dropLastWhile { it.isBlank() }
        return withoutEdgeBlanks.joinToString("\n").let { if (it.isEmpty()) "" else it + "\n" }
    }

    private fun collapseBlankRuns(lines: List<String>): List<String> =
        buildList {
            var blankRun = 0
            for (line in lines) {
                if (line.isBlank()) {
                    blankRun++
                    if (blankRun <= 1) {
                        add("")
                    }
                } else {
                    blankRun = 0
                    add(line)
                }
            }
        }

    private companion object {
        const val DECOMPILER_FIELD_MARKER = "\$FF:"
        val DECOMPILER_TAIL_COMMENT = Regex("""/\*\s*(compiled from|synthetic).*\*/""")
        val SYNTHETIC_ACCESS = Regex("""\w+\$\d+""")
        val IMPORT_LINE = Regex("""\s*import\s+\S.*""")
        val TRAILING_SPACES = Regex("""[ \t]+$""")
    }
}
