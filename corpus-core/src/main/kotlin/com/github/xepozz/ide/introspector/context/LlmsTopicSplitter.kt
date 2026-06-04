package com.github.xepozz.ide.introspector.context

data class LlmsTopic(val title: String, val body: String)

object LlmsTopicSplitter {
    private val HEADING = Regex("^# (.+)$")

    fun split(text: String): List<LlmsTopic> {
        val topics = mutableListOf<LlmsTopic>()
        var title: String? = null
        val body = StringBuilder()

        fun flush() {
            val current = title ?: return
            topics += LlmsTopic(current.trim(), body.toString().trim())
            body.setLength(0)
        }

        for (line in text.lineSequence()) {
            val heading = HEADING.matchEntire(line)
            if (heading != null) {
                flush()
                title = heading.groupValues[1]
            } else if (title != null) {
                body.appendLine(line)
            }
        }
        flush()
        return topics
    }
}
