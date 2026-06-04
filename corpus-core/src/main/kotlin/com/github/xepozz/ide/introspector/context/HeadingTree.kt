package com.github.xepozz.ide.introspector.context

data class HeadingNode(
    val level: Int,
    val title: String,
    val body: String,
    val children: List<HeadingNode>,
)

data class HeadingTree(val intro: String, val roots: List<HeadingNode>)

object HeadingTreeParser {
    private val HEADING = Regex("^(#{2,6})(?: (.*))?$")

    fun parse(text: String): HeadingTree {
        val intro = StringBuilder()
        val roots = mutableListOf<Builder>()
        val stack = ArrayDeque<Builder>()

        for (line in text.lineSequence()) {
            val match = HEADING.matchEntire(line)
            if (match == null) {
                if (stack.isEmpty()) intro.appendLine(line) else stack.last().body.appendLine(line)
                continue
            }
            val level = match.groupValues[1].length
            val node = Builder(level, match.groupValues[2].trim())
            while (stack.isNotEmpty() && stack.last().level >= level) stack.removeLast()
            if (stack.isEmpty()) roots += node else stack.last().children += node
            stack.addLast(node)
        }
        return HeadingTree(intro.toString().trim(), roots.map { it.build() })
    }

    private class Builder(val level: Int, val title: String) {
        val body = StringBuilder()
        val children = mutableListOf<Builder>()
        fun build(): HeadingNode = HeadingNode(level, title, body.toString().trim(), children.map { it.build() })
    }
}
