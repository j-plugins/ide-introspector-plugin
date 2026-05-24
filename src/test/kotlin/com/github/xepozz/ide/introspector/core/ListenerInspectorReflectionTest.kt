package com.github.xepozz.ide.introspector.core

import com.github.xepozz.ide.introspector.model.ListenerInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM tests for [ListenerInspector] reflective collection.
 *
 * The real platform types — `IdeaPluginDescriptorImpl`, `ContainerDescriptor`,
 * `ListenerDescriptor` — are `@ApiStatus.Internal` and field-renamed across versions,
 * so the inspector accesses them by name through [ExtensionPointInspector.readField].
 * The fixtures below model the *shapes* we care about (matching field names) without
 * needing the IntelliJ runtime — that's how we can drive every branch of
 * [ListenerInspector.collectFromDescriptor] / [ListenerInspector.toListenerInfoOrNull]
 * from a vanilla JUnit harness.
 *
 * ## How the fixtures work
 *
 * `collectFromDescriptor` reads `app` / `project` fields off the descriptor, each of
 * which is a `ContainerDescriptor` with a `listeners` field of `List<ListenerDescriptor>`.
 * We mirror that shape with [FakeIdeaPluginDescriptor] / [FakeContainerDescriptor] /
 * [FakeListenerDescriptor]. Tests then call `collectFromDescriptor` directly and assert
 * on the produced [ListenerInfo] list.
 *
 * The field-rename fallback is covered by [FakeListenerDescriptorAlt], which exposes
 * `topicClass` / `listenerClass` (older 251.x naming) instead of the canonical
 * `topicClassName` / `listenerClassName`.
 */
class ListenerInspectorReflectionTest {

    private val inspector = ListenerInspector()

    // ====================================================================================
    // SECTION 1. collectFromDescriptor — happy path
    // ====================================================================================

    @Test
    fun `collects one application-scope listener from a stub descriptor`() {
        val descriptor = FakeIdeaPluginDescriptor(
            app = FakeContainerDescriptor(
                listeners = listOf(FakeListenerDescriptor(
                    topicClassName = "com.example.TopicA",
                    listenerClassName = "com.example.ListenerA",
                )),
            ),
        )
        val out = mutableListOf<ListenerInfo>()

        inspector.collectFromDescriptor(descriptor, "com.example", "Example", out)

        assertEquals(1, out.size)
        val info = out.single()
        assertEquals("com.example.TopicA", info.topicClass)
        assertEquals("com.example.ListenerA", info.listenerClass)
        assertEquals("application", info.scope)
        assertEquals("com.example", info.providedByPluginId)
        assertEquals("Example", info.providedByPluginName)
        assertTrue("Defaults activeInTestMode=true when XML attribute absent", info.activeInTestMode)
        assertTrue("Defaults activeInHeadlessMode=true when XML attribute absent", info.activeInHeadlessMode)
        assertNull(info.os)
    }

    @Test
    fun `collects both application and project scope listeners`() {
        val descriptor = FakeIdeaPluginDescriptor(
            app = FakeContainerDescriptor(
                listeners = listOf(FakeListenerDescriptor("com.example.AppTopic", "com.example.AppListener")),
            ),
            project = FakeContainerDescriptor(
                listeners = listOf(FakeListenerDescriptor("com.example.ProjTopic", "com.example.ProjListener")),
            ),
        )
        val out = mutableListOf<ListenerInfo>()

        inspector.collectFromDescriptor(descriptor, "com.example", "Example", out)

        assertEquals(2, out.size)
        val app = out.single { it.scope == "application" }
        val proj = out.single { it.scope == "project" }
        assertEquals("com.example.AppTopic", app.topicClass)
        assertEquals("com.example.AppListener", app.listenerClass)
        assertEquals("com.example.ProjTopic", proj.topicClass)
        assertEquals("com.example.ProjListener", proj.listenerClass)
    }

    @Test
    fun `respects explicit activeInTestMode and activeInHeadlessMode flags`() {
        val descriptor = FakeIdeaPluginDescriptor(
            app = FakeContainerDescriptor(
                listeners = listOf(FakeListenerDescriptor(
                    topicClassName = "com.example.Topic",
                    listenerClassName = "com.example.Listener",
                    activeInTestMode = false,
                    activeInHeadlessMode = false,
                )),
            ),
        )
        val out = mutableListOf<ListenerInfo>()

        inspector.collectFromDescriptor(descriptor, "com.example", "Example", out)

        val info = out.single()
        assertEquals(false, info.activeInTestMode)
        assertEquals(false, info.activeInHeadlessMode)
    }

    @Test
    fun `surfaces the os attribute when present`() {
        val descriptor = FakeIdeaPluginDescriptor(
            app = FakeContainerDescriptor(
                listeners = listOf(FakeListenerDescriptor(
                    topicClassName = "com.example.Topic",
                    listenerClassName = "com.example.Listener",
                    os = "mac",
                )),
            ),
        )
        val out = mutableListOf<ListenerInfo>()

        inspector.collectFromDescriptor(descriptor, "com.example", "Example", out)

        assertEquals("mac", out.single().os)
    }

    // ====================================================================================
    // SECTION 2. collectFromDescriptor — edge cases
    // ====================================================================================

    @Test
    fun `skips entry when topicClassName is null and no fallback present`() {
        val descriptor = FakeIdeaPluginDescriptor(
            app = FakeContainerDescriptor(
                listeners = listOf(FakeListenerDescriptor(
                    topicClassName = null,
                    listenerClassName = "com.example.Listener",
                )),
            ),
        )
        val out = mutableListOf<ListenerInfo>()

        inspector.collectFromDescriptor(descriptor, "com.example", "Example", out)

        assertTrue("Malformed entry must be skipped silently, got $out", out.isEmpty())
    }

    @Test
    fun `skips entry when listenerClassName is null and no fallback present`() {
        val descriptor = FakeIdeaPluginDescriptor(
            app = FakeContainerDescriptor(
                listeners = listOf(FakeListenerDescriptor(
                    topicClassName = "com.example.Topic",
                    listenerClassName = null,
                )),
            ),
        )
        val out = mutableListOf<ListenerInfo>()

        inspector.collectFromDescriptor(descriptor, "com.example", "Example", out)

        assertTrue("Malformed entry must be skipped silently, got $out", out.isEmpty())
    }

    @Test
    fun `skips plugin entirely when app and project containers are missing`() {
        // Descriptor isn't an IdeaPluginDescriptorImpl shape — no app / project fields.
        // The inspector must skip without throwing.
        val descriptor = NotAnImplDescriptor()
        val out = mutableListOf<ListenerInfo>()

        inspector.collectFromDescriptor(descriptor, "com.example", "Example", out)

        assertTrue("Unknown descriptor shape must produce no entries, got $out", out.isEmpty())
    }

    @Test
    fun `handles container with null listeners field`() {
        val descriptor = FakeIdeaPluginDescriptor(
            app = FakeContainerDescriptor(listeners = null),
        )
        val out = mutableListOf<ListenerInfo>()

        inspector.collectFromDescriptor(descriptor, "com.example", "Example", out)

        assertTrue("Null listeners list must be treated as empty, got $out", out.isEmpty())
    }

    @Test
    fun `tolerates null entries inside the listeners list`() {
        val descriptor = FakeIdeaPluginDescriptor(
            app = FakeContainerDescriptor(
                listeners = listOf(
                    null,
                    FakeListenerDescriptor("com.example.Topic", "com.example.Listener"),
                ),
            ),
        )
        val out = mutableListOf<ListenerInfo>()

        inspector.collectFromDescriptor(descriptor, "com.example", "Example", out)

        assertEquals(1, out.size)
        assertEquals("com.example.Topic", out.single().topicClass)
    }

    // ====================================================================================
    // SECTION 3. Field-rename fallback (topicClass / listenerClass shapes)
    // ====================================================================================

    @Test
    fun `falls back to topicClass field when topicClassName absent`() {
        val descriptor = FakeIdeaPluginDescriptor(
            app = FakeContainerDescriptor(
                listeners = listOf(FakeListenerDescriptorAlt(
                    topicClass = "com.example.AltTopic",
                    listenerClass = "com.example.AltListener",
                )),
            ),
        )
        val out = mutableListOf<ListenerInfo>()

        inspector.collectFromDescriptor(descriptor, "com.example", "Example", out)

        assertEquals(1, out.size)
        val info = out.single()
        assertEquals("com.example.AltTopic", info.topicClass)
        assertEquals("com.example.AltListener", info.listenerClass)
    }

    // ====================================================================================
    // SECTION 4. toListenerInfoOrNull — direct unit invocations
    // ====================================================================================

    @Test
    fun `toListenerInfoOrNull returns null for entirely empty stub`() {
        val empty = EmptyStubListener()
        val result = inspector.toListenerInfoOrNull(empty, "application", "com.example", "Example")
        assertNull("A stub with no recognisable fields must yield null", result)
    }

    @Test
    fun `toListenerInfoOrNull preserves caller-supplied scope`() {
        val ld = FakeListenerDescriptor(
            topicClassName = "com.example.Topic",
            listenerClassName = "com.example.Listener",
        )
        val asApp = inspector.toListenerInfoOrNull(ld, "application", "com.example", "Example")
        val asProj = inspector.toListenerInfoOrNull(ld, "project", "com.example", "Example")
        assertEquals("application", asApp!!.scope)
        assertEquals("project", asProj!!.scope)
    }
}

// ========================================================================================
// Fixtures — shaped to match IdeaPluginDescriptorImpl / ContainerDescriptor / ListenerDescriptor
//
// The inspector reads fields by NAME via reflection; the JVM types of these stubs do not
// have to match the platform classes — only the field names do.
// ========================================================================================

private class FakeIdeaPluginDescriptor(
    @JvmField val app: FakeContainerDescriptor? = null,
    @JvmField val project: FakeContainerDescriptor? = null,
)

private class FakeContainerDescriptor(
    @JvmField val listeners: List<Any?>? = null,
)

/** Canonical shape — `topicClassName` / `listenerClassName` (current platform builds). */
private class FakeListenerDescriptor(
    @JvmField val topicClassName: String? = null,
    @JvmField val listenerClassName: String? = null,
    @JvmField val activeInTestMode: Boolean? = null,
    @JvmField val activeInHeadlessMode: Boolean? = null,
    @JvmField val os: String? = null,
)

/** Older platform shape — `topicClass` / `listenerClass`. Triggers the rename fallback. */
private class FakeListenerDescriptorAlt(
    @JvmField val topicClass: String? = null,
    @JvmField val listenerClass: String? = null,
)

/** Descriptor that doesn't expose `app` / `project` at all — covers the skip-the-plugin branch. */
private class NotAnImplDescriptor

/** Listener descriptor with no recognisable field — covers the early-return in toListenerInfoOrNull. */
private class EmptyStubListener
