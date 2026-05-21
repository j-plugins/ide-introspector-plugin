import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.changelog")
}

dependencies {
    // kotlinx-serialization-json is bundled with the IDE (and with the com.intellij.mcpServer
    // plugin). We must use the platform copy at runtime — bundling our own creates two
    // independent KSerializer classloaders, which makes serializerOrNull(KType) return null
    // for our @Serializable data classes when MCP's reflection-based bridge looks them up,
    // and the bridge then throws "Result type X is not serializable".
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Phase 2: Kotlin runtime execution via JSR-223 ScriptEngine.
    // kotlin-scripting-jsr223 pulls kotlin-compiler-embeddable transitively, which gives us
    // a self-contained compiler inside the plugin's classloader, isolated from the IDE's
    // bundled Kotlin plugin.
    implementation("org.jetbrains.kotlin:kotlin-scripting-jsr223:2.1.20")

    testImplementation("junit:junit:4.13.2")

    intellijPlatform {
        intellijIdea("2025.2.6.2")
        bundledPlugin("org.jetbrains.kotlin")
        plugin("com.intellij.mcpServer", "252.28238.29")
        testFramework(TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild.set("252")
            untilBuild.set("253.*")
        }
    }
}

kotlin {
    jvmToolchain(21)
}

// -------------------------------------------------------------------------------------
// docs/MCP_TOOLS.md generator.
//
// Single source of truth for MCP tool descriptions is the @McpTool / @McpDescription
// annotations on the McpToolset classes themselves. This task parses those source files
// at build time and dumps a human-readable markdown reference, so we don't have to
// duplicate descriptions in README.md / wiki / docs / blog posts.
//
// The parser is intentionally simple: regex over Kotlin source files, matching the
// fixed annotation styles we use (tool-level @McpTool + @McpDescription, parameter-level
// @McpDescription). Not a full Kotlin parser — if you start putting comments inside the
// annotation strings or using other styles, extend the regex below.
// -------------------------------------------------------------------------------------
tasks.register("generateToolsDoc") {
    group = "documentation"
    description = "Extracts @McpTool / @McpDescription annotations from MCP toolset sources and writes docs/MCP_TOOLS.md."

    val toolsSrcDir = layout.projectDirectory.dir("src/main/kotlin/com/github/xepozz/introspectorplugin/tools")
    val toolsDocFile = layout.projectDirectory.file("docs/MCP_TOOLS.md")
    val rootDirPath = rootDir.absolutePath

    inputs.dir(toolsSrcDir).withPropertyName("toolsSrcDir")
    outputs.file(toolsDocFile).withPropertyName("toolsDocFile")

    doLast {
        // Everything below is inline (no top-level helpers, no local data classes — Kotlin
        // Gradle DSL's IR backend chokes on them) so configuration cache can serialize the
        // task without snapshotting captured Project state.
        //
        // Tools are represented as Map<String, Any?> with these keys:
        //   "file" (String), "class" (String), "name" (String), "description" (String),
        //   "params" (List<Triple<name, type, description>>), "returnType" (String)

        fun splitTopLevelCommas(s: String): List<String> {
            val out = mutableListOf<String>()
            var depthParen = 0
            var depthAngle = 0
            var inTriple = false
            var inSingle = false
            val cur = StringBuilder()
            var i = 0
            while (i < s.length) {
                val c = s[i]
                val nextThree = if (i + 3 <= s.length) s.substring(i, i + 3) else ""
                when {
                    inTriple -> {
                        cur.append(c)
                        if (nextThree == "\"\"\"") { cur.append("\"\""); i += 2; inTriple = false }
                    }
                    inSingle -> {
                        cur.append(c)
                        if (c == '"' && s.getOrNull(i - 1) != '\\') inSingle = false
                    }
                    nextThree == "\"\"\"" -> { cur.append(nextThree); i += 2; inTriple = true }
                    c == '"' -> { cur.append(c); inSingle = true }
                    c == '(' -> { depthParen++; cur.append(c) }
                    c == ')' -> { depthParen--; cur.append(c) }
                    c == '<' -> { depthAngle++; cur.append(c) }
                    c == '>' -> { depthAngle--; cur.append(c) }
                    c == ',' && depthParen == 0 && depthAngle == 0 -> { out += cur.toString(); cur.clear() }
                    else -> cur.append(c)
                }
                i++
            }
            if (cur.isNotEmpty()) out += cur.toString()
            return out
        }

        fun parseParameters(raw: String): List<Triple<String, String, String>> {
            val out = mutableListOf<Triple<String, String, String>>()
            val items = splitTopLevelCommas(raw)
            val paramRegex = Regex(
                """(?:@McpDescription\s*\(\s*("{3}([\s\S]*?)"{3}|"((?:[^"\\]|\\.)*)")\s*\)\s*)?""" +
                """(\w+)\s*:\s*([^=]+?)(?:\s*=\s*[\s\S]+)?$"""
            )
            for (item in items) {
                val cleaned = item.trim().trimEnd(',').trim()
                if (cleaned.isEmpty()) continue
                val m = paramRegex.matchEntire(cleaned) ?: continue
                val triple = m.groupValues[2].ifEmpty { null }
                val single = m.groupValues[3].ifEmpty { null }
                val descRaw = triple ?: single.orEmpty()
                out += Triple(
                    m.groupValues[4],
                    m.groupValues[5].trim(),
                    descRaw.trimMargin().trim(),
                )
            }
            return out
        }

        val tools = mutableListOf<Map<String, Any>>()
        toolsSrcDir.asFile.walkTopDown().filter { it.isFile && it.extension == "kt" }.forEach { f ->
            val text = f.readText()
            val toolsetClassName = Regex("""class\s+(\w+)\s*:\s*McpToolset""").find(text)?.groupValues?.get(1)
                ?: return@forEach
            val toolRegex = Regex(
                """@McpTool\s*(?:\(\s*name\s*=\s*"([^"]+)"\s*\))?\s*""" +
                """@McpDescription\s*\(\s*("{3}([\s\S]*?)"{3}|"((?:[^"\\]|\\.)*)")\s*\)\s*""" +
                """suspend\s+fun\s+`?(\w+)`?\s*\(([\s\S]*?)\)\s*:\s*([\w<>?., ]+?)\s*[={]"""
            )
            for (m in toolRegex.findAll(text)) {
                val explicitName = m.groupValues[1].ifEmpty { null }
                val triple = m.groupValues[3].ifEmpty { null }
                val single = m.groupValues[4].ifEmpty { null }
                val methodName = m.groupValues[5]
                val rawDesc = triple ?: single.orEmpty()
                tools += mapOf(
                    "file" to f.absolutePath.removePrefix(rootDirPath).trimStart('/'),
                    "class" to toolsetClassName,
                    "name" to (explicitName ?: methodName),
                    "description" to rawDesc.trimMargin().trim(),
                    "params" to parseParameters(m.groupValues[6]),
                    "returnType" to m.groupValues[7].trim(),
                )
            }
        }
        tools.sortBy { it["name"] as String }

        fun anchor(toolName: String) = toolName.lowercase().replace('.', '-').replace(Regex("[^a-z0-9-]"), "")

        val md = buildString {
            appendLine("<!-- AUTO-GENERATED by ./gradlew generateToolsDoc. Do not edit by hand — change the source-level @McpDescription annotations instead. -->")
            appendLine()
            appendLine("# IDE Introspect MCP — Tool Reference")
            appendLine()
            appendLine("Generated from the `@McpTool` / `@McpDescription` annotations on the `McpToolset`")
            appendLine("classes under `src/main/kotlin/com/github/xepozz/introspectorplugin/tools/`.")
            appendLine("Re-run `./gradlew generateToolsDoc` (or any `./gradlew build`) to refresh.")
            appendLine()
            appendLine("**Total tools:** ${tools.size}")
            appendLine()
            appendLine("## Tools by group")
            appendLine()
            val grouped = tools.groupBy { (it["name"] as String).substringBefore('.', missingDelimiterValue = "other") }
            for ((group, list) in grouped.entries.sortedBy { it.key }) {
                appendLine("### `$group.*` (${list.size})")
                appendLine()
                for (t in list) {
                    val n = t["name"] as String
                    appendLine("- [`$n`](#${anchor(n)})")
                }
                appendLine()
            }
            appendLine("---")
            appendLine()
            for (t in tools) {
                val n = t["name"] as String
                appendLine("## `$n`")
                appendLine()
                appendLine("*${t["class"]} — `${t["file"]}`*")
                appendLine()
                appendLine(t["description"] as String)
                appendLine()
                @Suppress("UNCHECKED_CAST")
                val params = t["params"] as List<Triple<String, String, String>>
                if (params.isNotEmpty()) {
                    appendLine("**Parameters**")
                    appendLine()
                    appendLine("| Name | Type | Description |")
                    appendLine("| --- | --- | --- |")
                    for ((pn, pt, pd) in params) {
                        val escDesc = pd.replace("|", "\\|").replace("\n", " ")
                        appendLine("| `$pn` | `${pt.replace("|", "\\|")}` | $escDesc |")
                    }
                    appendLine()
                }
                appendLine("**Returns:** `${t["returnType"]}`")
                appendLine()
                appendLine("---")
                appendLine()
            }
        }

        val target = toolsDocFile.asFile
        target.parentFile.mkdirs()
        target.writeText(md)
        logger.lifecycle("Wrote ${tools.size} MCP tool descriptions to ${target.absolutePath.removePrefix(rootDirPath).trimStart('/')}")
    }
}

tasks.named("build") {
    dependsOn("generateToolsDoc")
}
