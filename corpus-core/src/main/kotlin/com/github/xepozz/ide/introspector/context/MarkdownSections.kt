package com.github.xepozz.ide.introspector.context

data class MarkdownSection(val title: String, val body: String)

data class MarkdownSplit(val intro: String, val sections: List<MarkdownSection>)

object MarkdownSections {
    fun split(body: String, level: Int = 2): MarkdownSplit {
        val heading = Regex("^" + "#".repeat(level) + "(?: (.*))?$")
        val intro = StringBuilder()
        val sections = mutableListOf<MarkdownSection>()
        var title: String? = null
        val section = StringBuilder()

        fun flush() {
            val current = title ?: return
            sections += MarkdownSection(current.trim(), section.toString().trim())
            section.setLength(0)
        }

        for (line in body.lineSequence()) {
            val match = heading.matchEntire(line)
            when {
                match != null -> {
                    flush()
                    title = match.groupValues[1]
                }
                title == null -> intro.appendLine(line)
                else -> section.appendLine(line)
            }
        }
        flush()
        return MarkdownSplit(intro.toString().trim(), sections)
    }
}
