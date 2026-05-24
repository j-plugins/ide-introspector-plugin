package com.github.xepozz.ide.introspector.core

import com.github.xepozz.ide.introspector.model.TopicInfo
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.util.messages.Topic
import java.io.File
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.util.jar.JarFile

/**
 * Discovers [Topic] instances declared by each plugin by walking the plugin's classpath and
 * looking for `Topic`-typed fields. We deliberately load classes with `initialize=false`
 * so we never trigger the plugin's `<clinit>` blocks just to enumerate metadata — that
 * means the Topic *value* (and thus its `displayName` / `broadcastDirection`) is never
 * read, only the field's declared generic type and owner.
 *
 * Discovered patterns:
 *   1. `@JvmField val TOPIC: Topic<L>` in a Kotlin interface companion — produces a static
 *      field on the outer interface (recommended IDE convention).
 *   2. `public static final Topic<L> TOPIC` in Java — same shape.
 *   3. `val TOPIC: Topic<L>` (no `@JvmField`) in a Kotlin companion — produces an instance
 *      field on the `$Companion` nested class.
 *
 * Out of scope:
 *   * Topics constructed at runtime (lazy properties, factories) — they have no static
 *     declaration to scan.
 *   * Topic instances whose listener generic type was erased at compile time (very rare;
 *     Kotlin always emits a Signature attribute).
 */
object TopicInspector {

    /** Per-plugin scan cap — most plugin main jars have ≤2000 classes; bigger ones get truncated. */
    private const val MAX_CLASSES_PER_PLUGIN = 5_000

    /**
     * Jar-name prefixes for well-known runtime libraries that ship in plugin `lib/` dirs but
     * never declare IntelliJ message-bus Topics. Skipping them avoids burning the per-plugin
     * scan budget on tens of thousands of irrelevant classes (`kotlin-compiler-embeddable`
     * alone is ~25k classes). If we wrongly skip something, the worst case is that the topic
     * doesn't appear in Platform Explorer — never a runtime error.
     */
    private val SKIP_JAR_PREFIXES = setOf(
        "kotlin-compiler-embeddable",
        "kotlin-daemon-embeddable",
        "kotlin-reflect",
        "kotlin-script-",
        "kotlin-scripting-",
        "kotlin-stdlib",
        "kotlinx-coroutines",
        "kotlinx-serialization",
        "annotations-",
    )

    fun listAll(): List<TopicInfo> {
        val out = mutableListOf<TopicInfo>()
        for (descriptor in PluginManagerCore.plugins) {
            collectFor(descriptor, out)
        }
        return out
    }

    fun listForPlugin(descriptor: IdeaPluginDescriptor): List<TopicInfo> {
        val out = mutableListOf<TopicInfo>()
        collectFor(descriptor, out)
        return out
    }

    private fun collectFor(descriptor: IdeaPluginDescriptor, out: MutableList<TopicInfo>) {
        val classLoader = descriptor.classLoader
        val pluginId = descriptor.pluginId.idString
        val pluginName = descriptor.name
        val classes = try {
            enumeratePluginClasses(descriptor).take(MAX_CLASSES_PER_PLUGIN).toList()
        } catch (t: Throwable) {
            thisLogger().info("Topic scan: enumeration failed for $pluginId: ${t.message}")
            return
        }
        // Same topic may be reachable via the outer interface (synthetic static accessor field
        // that Kotlin generates for companion vals in some versions) AND via its `$Companion`
        // instance field. Dedup by id so callers see one entry per logical topic.
        val seen = mutableSetOf<String>()
        val before = out.size
        for (fqn in classes) {
            try {
                scanClassForTopics(fqn, classLoader, pluginId, pluginName, seen, out)
            } catch (_: Throwable) {
                // Class might be unloadable due to missing transitive deps — skip silently.
            }
        }
        thisLogger().info(
            "Topic scan: plugin=$pluginId path=${descriptor.pluginPath} classes=${classes.size} topics=${out.size - before}"
        )
    }

    private fun scanClassForTopics(
        fqn: String,
        classLoader: ClassLoader,
        pluginId: String,
        pluginName: String?,
        seen: MutableSet<String>,
        out: MutableList<TopicInfo>,
    ) {
        val cls = try {
            Class.forName(fqn, false, classLoader)
        } catch (_: Throwable) {
            return
        }
        val isCompanion = fqn.endsWith("\$Companion")
        // For Kotlin companions the *outer* class is what we want to report as the
        // declaring class — that's what callers grep for.
        val outerName = if (isCompanion) fqn.substringBeforeLast("\$Companion") else fqn
        for (field in cls.declaredFields) {
            if (field.type != Topic::class.java) continue
            // On the *outer* class we only care about static fields (`@JvmField` or Java
            // `public static final`). On the `$Companion` class Kotlin may emit the val
            // as static OR instance depending on the version — accept both there.
            if (!isCompanion && !Modifier.isStatic(field.modifiers)) continue

            val listenerFqn = listenerTypeOf(field) ?: continue
            val id = "$outerName.${field.name}"
            if (!seen.add(id)) continue
            out += TopicInfo(
                id = id,
                declaringClassName = outerName,
                fieldName = field.name,
                listenerClassName = listenerFqn,
                onCompanion = isCompanion,
                providedByPluginId = pluginId,
                providedByPluginName = pluginName,
            )
        }
    }

    /** Reads `Topic<L>`'s `L` from the field's generic signature attribute. */
    private fun listenerTypeOf(field: java.lang.reflect.Field): String? {
        val generic = field.genericType as? ParameterizedType ?: return null
        val arg = generic.actualTypeArguments.firstOrNull() ?: return null
        return when (arg) {
            is Class<*> -> arg.name
            is ParameterizedType -> (arg.rawType as? Class<*>)?.name
            else -> null
        }
    }

    private fun enumeratePluginClasses(descriptor: IdeaPluginDescriptor): Sequence<String> {
        val nioPath = descriptor.pluginPath ?: return emptySequence()
        // Use java.io.File rather than java.nio.Files — IntelliJ replaces the default NIO
        // FileSystemProvider (MultiRoutingFileSystemProvider) and Files.list/walk can return
        // empty results for paths that exist on disk but aren't routed.
        val root = File(nioPath.toString())
        val result = LinkedHashSet<String>()
        when {
            root.isFile && root.name.endsWith(".jar") -> collectFromJar(root, result)
            root.isDirectory -> {
                val lib = File(root, "lib")
                val jars = (lib.listFiles() ?: emptyArray())
                    .filter { it.isFile && it.name.endsWith(".jar") }
                    .filterNot { jar -> SKIP_JAR_PREFIXES.any { jar.name.startsWith(it) } }
                // The plugin's main jar is typically named "<pluginDir>-<version>.jar".
                // Process it first so even if the per-plugin cap kicks in, we cover the
                // plugin's own code before transitive bundled jars.
                val pluginDirName = root.name
                val (own, others) = jars.partition { it.name.startsWith(pluginDirName) }
                (own + others.sortedBy { it.name }).forEach { jar ->
                    collectFromJar(jar, result)
                }
                // Loose .class files (rare — IntelliJ unpacks plugins as jars).
                root.walkTopDown().forEach { entry ->
                    if (entry.isFile && entry.name.endsWith(".class")) {
                        val rel = entry.relativeTo(root).invariantSeparatorsPath
                        result += rel.removeSuffix(".class").replace('/', '.')
                    }
                }
            }
        }
        return result.asSequence()
    }

    private fun collectFromJar(jarFile: File, into: MutableSet<String>) {
        try {
            JarFile(jarFile).use { jf ->
                val entries = jf.entries()
                while (entries.hasMoreElements()) {
                    val e = entries.nextElement()
                    val name = e.name
                    if (!name.endsWith(".class")) continue
                    // Skip module-info / package-info — they have no Topic fields.
                    if (name.endsWith("module-info.class") || name.endsWith("package-info.class")) continue
                    into += name.removeSuffix(".class").replace('/', '.')
                }
            }
        } catch (_: Throwable) {
        }
    }
}
