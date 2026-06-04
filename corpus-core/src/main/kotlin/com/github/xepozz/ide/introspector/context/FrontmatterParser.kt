package com.github.xepozz.ide.introspector.context

private const val FENCE = "---"
private const val BYTE_ORDER_MARK = '﻿'

private val NON_ID_CHARACTER = Regex("[^a-z0-9._-]")

private enum class ParsedValueKind { STRING, LIST }

private data class ParsedHeaderValue(
    val kind: ParsedValueKind,
    val stringValue: String,
    val listValue: List<String>,
)

object FrontmatterParser {
    fun parse(rawText: String, sourcePath: String): ParsedDocument {
        val normalizedText = normalize(rawText)
        val lines = normalizedText.split("\n")

        var index = 0
        while (index < lines.size && lines[index].isBlank()) {
            index++
        }

        if (index >= lines.size || lines[index].trim() != FENCE) {
            return ParsedDocument(
                frontmatter = null,
                body = normalizedText,
                parseIssues = listOf(
                    ValidationIssue(
                        severity = Severity.WARNING,
                        code = IssueCode.MISSING_FRONTMATTER,
                        message = "No frontmatter block found; document must begin with a '---' fence.",
                        sourcePath = sourcePath,
                    ),
                ),
            )
        }

        val openingFenceIndex = index
        var closingFenceIndex = -1
        var scanIndex = openingFenceIndex + 1
        while (scanIndex < lines.size) {
            if (lines[scanIndex].trim() == FENCE) {
                closingFenceIndex = scanIndex
                break
            }
            scanIndex++
        }

        if (closingFenceIndex == -1) {
            val bodyAfterOpening = lines.subList(openingFenceIndex + 1, lines.size).joinToString("\n")
            return ParsedDocument(
                frontmatter = null,
                body = bodyAfterOpening,
                parseIssues = listOf(
                    ValidationIssue(
                        severity = Severity.ERROR,
                        code = IssueCode.UNTERMINATED_FRONTMATTER,
                        message = "Frontmatter opening fence has no matching closing '---'.",
                        sourcePath = sourcePath,
                    ),
                ),
            )
        }

        val headerLines = lines.subList(openingFenceIndex + 1, closingFenceIndex)
        val body = extractBody(lines, closingFenceIndex)

        return buildDocument(headerLines, body, sourcePath)
    }

    private fun normalize(rawText: String): String {
        val withoutByteOrderMark = rawText.removePrefix(BYTE_ORDER_MARK.toString())
        return withoutByteOrderMark
            .replace("\r\n", "\n")
            .replace("\r", "\n")
    }

    private fun extractBody(lines: List<String>, closingFenceIndex: Int): String {
        val afterFence = lines.subList(closingFenceIndex + 1, lines.size).joinToString("\n")
        return afterFence.removePrefix("\n")
    }

    private fun buildDocument(
        headerLines: List<String>,
        body: String,
        sourcePath: String,
    ): ParsedDocument {
        val parseIssues = mutableListOf<ValidationIssue>()
        val rawValuesByKey = linkedMapOf<String, ParsedHeaderValue>()
        val unknownKeys = mutableListOf<String>()

        for (line in headerLines) {
            if (line.isBlank()) {
                continue
            }

            val colonIndex = line.indexOf(':')
            if (colonIndex < 0) {
                parseIssues += malformedLine(line, sourcePath)
                continue
            }

            val key = line.substring(0, colonIndex).trim()
            if (key.isEmpty()) {
                parseIssues += malformedLine(line, sourcePath)
                continue
            }

            val rawValue = line.substring(colonIndex + 1).trim()
            val parsedValue = parseHeaderValue(rawValue, key, parseIssues, sourcePath)

            if (rawValuesByKey.containsKey(key)) {
                parseIssues += ValidationIssue(
                    severity = Severity.WARNING,
                    code = IssueCode.DUPLICATE_KEY,
                    message = "Duplicate key '$key'; last value wins.",
                    key = key,
                    sourcePath = sourcePath,
                )
            }

            if (key !in WIRE_KEYS && key !in unknownKeys) {
                unknownKeys += key
                parseIssues += ValidationIssue(
                    severity = Severity.WARNING,
                    code = IssueCode.UNKNOWN_KEY,
                    message = "Unknown frontmatter key '$key'.",
                    key = key,
                    sourcePath = sourcePath,
                )
            }

            rawValuesByKey[key] = parsedValue
        }

        val frontmatter = assembleFrontmatter(rawValuesByKey, unknownKeys, sourcePath, parseIssues)
        return ParsedDocument(frontmatter = frontmatter, body = body, parseIssues = parseIssues)
    }

    private fun parseHeaderValue(
        rawValue: String,
        key: String,
        parseIssues: MutableList<ValidationIssue>,
        sourcePath: String,
    ): ParsedHeaderValue {
        if (!rawValue.startsWith("[")) {
            return ParsedHeaderValue(ParsedValueKind.STRING, rawValue, emptyList())
        }

        if (!rawValue.endsWith("]")) {
            parseIssues += ValidationIssue(
                severity = Severity.ERROR,
                code = IssueCode.MALFORMED_LIST,
                message = "Malformed list for key '$key': missing closing ']'.",
                key = key,
                sourcePath = sourcePath,
            )
            return ParsedHeaderValue(ParsedValueKind.LIST, "", emptyList())
        }

        val inner = rawValue.substring(1, rawValue.length - 1).trim()
        if (inner.isEmpty()) {
            return ParsedHeaderValue(ParsedValueKind.LIST, "", emptyList())
        }

        val elements = inner.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        return ParsedHeaderValue(ParsedValueKind.LIST, "", elements)
    }

    private fun assembleFrontmatter(
        rawValuesByKey: Map<String, ParsedHeaderValue>,
        unknownKeys: List<String>,
        sourcePath: String,
        parseIssues: MutableList<ValidationIssue>,
    ): Frontmatter {
        val explicitId = rawValuesByKey["id"]?.stringValue?.takeIf { it.isNotEmpty() }
        val idDerivedFromPath = explicitId == null
        val id = explicitId ?: deriveId(sourcePath)

        return Frontmatter(
            id = id,
            title = stringOf(rawValuesByKey, "title"),
            source = enumOf(rawValuesByKey, "source", "source", parseIssues, sourcePath) { Source.valueOf(it) },
            kind = enumOf(rawValuesByKey, "kind", "kind", parseIssues, sourcePath) { Kind.valueOf(it) },
            state = enumOf(rawValuesByKey, "state", "state", parseIssues, sourcePath) { State.valueOf(it) }
                ?: State.VERIFIED,
            description = stringOf(rawValuesByKey, "description"),
            whenToUse = stringOf(rawValuesByKey, "when_to_use"),
            verifiedAgainstBuild = stringOf(rawValuesByKey, "verifiedAgainstBuild"),
            tags = listOf(rawValuesByKey, "tags"),
            relatedExtensionPoints = listOf(rawValuesByKey, "related_eps"),
            relatedTools = listOf(rawValuesByKey, "related_tools"),
            plugin = stringOf(rawValuesByKey, "plugin"),
            extensionPoint = stringOf(rawValuesByKey, "ep"),
            unknownKeys = unknownKeys.toList(),
            idDerivedFromPath = idDerivedFromPath,
        )
    }

    private fun stringOf(rawValuesByKey: Map<String, ParsedHeaderValue>, wireKey: String): String? {
        val parsedValue = rawValuesByKey[wireKey] ?: return null
        if (parsedValue.kind != ParsedValueKind.STRING) {
            return null
        }
        return parsedValue.stringValue.takeIf { it.isNotEmpty() }
    }

    private fun listOf(rawValuesByKey: Map<String, ParsedHeaderValue>, wireKey: String): List<String> {
        val parsedValue = rawValuesByKey[wireKey] ?: return emptyList()
        if (parsedValue.kind == ParsedValueKind.LIST) {
            return parsedValue.listValue
        }
        val single = parsedValue.stringValue.trim()
        return if (single.isEmpty()) emptyList() else listOf(single)
    }

    private fun <T> enumOf(
        rawValuesByKey: Map<String, ParsedHeaderValue>,
        wireKey: String,
        issueKey: String,
        parseIssues: MutableList<ValidationIssue>,
        sourcePath: String,
        resolve: (String) -> T,
    ): T? {
        val rawString = stringOf(rawValuesByKey, wireKey) ?: return null
        val candidate = rawString.trim().uppercase()
        return runCatching { resolve(candidate) }.getOrElse {
            parseIssues += ValidationIssue(
                severity = Severity.ERROR,
                code = IssueCode.INVALID_ENUM,
                message = "Invalid value '$rawString' for key '$issueKey'.",
                key = issueKey,
                sourcePath = sourcePath,
            )
            null
        }
    }

    private fun deriveId(sourcePath: String): String {
        val withoutExtension = sourcePath.substringBeforeLast('.', sourcePath)
        return withoutExtension
            .replace('/', '.')
            .replace('\\', '.')
            .lowercase()
            .replace(NON_ID_CHARACTER, "-")
    }

    private fun malformedLine(line: String, sourcePath: String): ValidationIssue =
        ValidationIssue(
            severity = Severity.ERROR,
            code = IssueCode.MALFORMED_LINE,
            message = "Malformed frontmatter line: '$line'.",
            key = null,
            sourcePath = sourcePath,
        )
}

private val WIRE_KEYS = setOf(
    "id",
    "title",
    "source",
    "kind",
    "state",
    "description",
    "when_to_use",
    "verifiedAgainstBuild",
    "tags",
    "related_eps",
    "related_tools",
    "plugin",
    "ep",
)
