package com.github.xepozz.ide.introspector.core.platform

import com.github.xepozz.ide.introspector.core.PsiOutlineCollector
import com.github.xepozz.ide.introspector.model.GetOutlineResponse
import com.github.xepozz.ide.introspector.model.OutlineNode
import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Platform-level tests for [PsiOutlineCollector]. Each test feeds a real PsiFile through the
 * collector and asserts the returned [GetOutlineResponse] reflects the file's declaration tree
 * — matching what IntelliJ's Structure tool window would show.
 *
 * Includes a regression for the formerly no-op `includeInherited` flag (Finding 2): when
 * `includeInherited=true` on a Java class with a superclass, the response must include at
 * least one inherited member node (or a warning explaining why none was contributed).
 */
class PsiOutlineCollectorPlatformTest : BasePlatformTestCase() {

    private fun <T> read(block: () -> T): T {
        val ref = arrayOfNulls<Any>(1)
        ApplicationManager.getApplication().runReadAction {
            @Suppress("UNCHECKED_CAST")
            ref[0] = block() as Any?
        }
        @Suppress("UNCHECKED_CAST")
        return ref[0] as T
    }

    private fun configure(source: String, fileName: String = "Foo.java") {
        myFixture.configureByText(fileName, source)
    }

    private fun outline(
        includeFields: Boolean = true,
        includeInherited: Boolean = false,
        maxDepth: Int = 6,
        maxNodes: Int = 500,
    ): GetOutlineResponse = read {
        PsiOutlineCollector.collect(
            psiFile = myFixture.file,
            includeFields = includeFields,
            includeInherited = includeInherited,
            maxDepth = maxDepth,
            maxNodes = maxNodes,
        )
    }

    private fun flatten(nodes: List<OutlineNode>): List<OutlineNode> =
        nodes.flatMap { listOf(it) + flatten(it.children) }

    // ============================================================================
    // Java outline
    // ============================================================================

    fun testJavaOutlineHasClassMethodsAndFields() {
        configure(
            """
            class Foo {
                private int count;
                public String name;
                void bar() {}
                int compute() { return 0; }
            }
            """.trimIndent(),
        )
        val resp = outline()

        // One top-level class node.
        assertEquals("expected 1 top-level node (the class), got ${resp.nodes.size}", 1, resp.nodes.size)
        val classNode = resp.nodes.single()
        assertEquals("class", classNode.kind)
        assertEquals("Foo", classNode.name)
        // FQN is the simple name (default package).
        assertEquals("Foo", classNode.fqn)

        // Children: 2 fields + 2 methods.
        val kindsByName = classNode.children.associate { it.name to it.kind }
        assertEquals("field", kindsByName["count"])
        assertEquals("field", kindsByName["name"])
        assertEquals("method", kindsByName["bar"])
        assertEquals("method", kindsByName["compute"])

        // Modifier extraction for the private field.
        val countField = classNode.children.first { it.name == "count" }
        assertTrue("expected 'private' modifier on count; got ${countField.modifiers}", "private" in countField.modifiers)
        assertEquals("int", countField.typeText)
    }

    fun testJavaOutlineHasInnerClass() {
        configure(
            """
            class Outer {
                static class Inner {
                    void innerMethod() {}
                }
            }
            """.trimIndent(),
        )
        val resp = outline()
        val outer = resp.nodes.single()
        val inner = outer.children.firstOrNull { it.name == "Inner" }
        assertNotNull("expected nested Inner class in outline children", inner)
        assertEquals("class", inner!!.kind)
        val innerMethod = inner.children.firstOrNull { it.name == "innerMethod" }
        assertNotNull("expected Inner.innerMethod() in outline", innerMethod)
    }

    fun testIncludeFieldsFalseDropsFields() {
        configure(
            """
            class Foo {
                int count;
                void bar() {}
            }
            """.trimIndent(),
        )
        val resp = outline(includeFields = false)
        val classNode = resp.nodes.single()
        val kinds = classNode.children.map { it.kind }
        assertFalse("includeFields=false must exclude 'field' nodes; got $kinds", "field" in kinds)
        assertTrue("but method nodes must remain", "method" in kinds)
    }

    fun testMaxNodesTruncation() {
        // Synthesise a class with many methods to push past the cap.
        val methods = (1..50).joinToString("\n    ") { "void m$it() {}" }
        configure("class Foo {\n    $methods\n}")
        val resp = outline(maxNodes = 10)

        assertTrue("expected truncated=true when methods exceed maxNodes", resp.truncated)
        assertTrue(
            "nodeCount must respect the cap (got ${resp.nodeCount}, cap=10)",
            resp.nodeCount <= 10,
        )
        assertTrue(
            "warnings must mention truncation; got ${resp.warnings}",
            resp.warnings.any { it.contains("truncated") },
        )
    }

    // ============================================================================
    // Kotlin outline
    // ============================================================================

    fun testKotlinOutlineHasTopLevelFunctionAndClass() {
        configure(
            """
            package demo

            fun greet() = "hi"

            class Greeter {
                fun shout() = "HI"
            }
            """.trimIndent(),
            fileName = "Foo.kt",
        )
        val resp = outline()

        // Kotlin file's structure view exposes top-level `greet` AND `Greeter` (and may also
        // expose a wrapper file node depending on platform version). We assert the contents
        // are present somewhere in the flattened tree, not the exact root shape.
        val all = flatten(resp.nodes)
        val names = all.map { it.name }.toSet()
        assertTrue("expected top-level function 'greet'; got $names", "greet" in names)
        assertTrue("expected top-level class 'Greeter'; got $names", "Greeter" in names)
        assertTrue("expected member 'shout'; got $names", "shout" in names)

        // The Kotlin classifier should land class/method kinds.
        val greeter = all.first { it.name == "Greeter" }
        assertEquals("class", greeter.kind)
        val shout = all.first { it.name == "shout" }
        assertEquals("method", shout.kind)
    }

    // ============================================================================
    // includeInherited — regression for Finding 2
    // ============================================================================

    fun testIncludeInheritedTrueSurfacesUserSuperclassMembers() {
        // Regression for Finding 2: pre-fix, `includeInherited=true` was a no-op — the toggle
        // returned the same tree as `false`. Using a user-defined Base class (not java.lang.Object)
        // sidesteps any mock-JDK indexing limitations of BasePlatformTestCase: the Base and Sub
        // classes live in the same in-memory PSI, so JavaInheritedMembersNodeProvider can find
        // baseMethod() / baseField via the project's own PsiClass chain.
        myFixture.addFileToProject(
            "Base.java",
            """
            public class Base {
                public int baseField;
                public void baseMethod() {}
            }
            """.trimIndent(),
        )
        myFixture.configureByText(
            "Sub.java",
            """
            public class Sub extends Base {
                public void subMethod() {}
            }
            """.trimIndent(),
        )

        val withInherited = outline(includeInherited = true)
        val withoutInherited = outline(includeInherited = false)

        val withInheritedNames = flatten(withInherited.nodes).map { it.name }.toSet()
        val withoutInheritedNames = flatten(withoutInherited.nodes).map { it.name }.toSet()

        // Both must contain the declared subMethod.
        assertTrue("subMethod must always appear", "subMethod" in withInheritedNames)
        assertTrue("subMethod must always appear", "subMethod" in withoutInheritedNames)

        // The regression: pre-fix, the two sets were IDENTICAL. Post-fix, includeInherited=true
        // must either add nodes (the typical IntelliJ-IDE path) OR emit a warning explaining
        // why no provider contributed. Silent equality between the two modes is the bug.
        val added = withInheritedNames - withoutInheritedNames
        val warned = withInherited.warnings.any { it.contains("includeInherited") }
        assertTrue(
            "includeInherited=true must either add nodes or emit a warning; " +
                "added=$added warnings=${withInherited.warnings}",
            added.isNotEmpty() || warned,
        )
        // When the provider contributed, baseMethod (and/or baseField) are the expected hits.
        if (added.isNotEmpty()) {
            assertTrue(
                "expected baseMethod or baseField among inherited additions; got $added",
                "baseMethod" in added || "baseField" in added,
            )
        }
    }

    fun testIncludeInheritedFalseStillExcludesInherited() {
        configure("class Foo { void bar() {} }")
        val resp = outline(includeInherited = false)
        val names = flatten(resp.nodes).map { it.name }
        // Only the declared `bar` method — no toString/hashCode leaks.
        assertTrue("expected declared 'bar' method", "bar" in names)
        assertFalse("includeInherited=false must NOT surface inherited Object methods", "toString" in names)
        assertFalse("includeInherited=false must NOT surface inherited Object methods", "hashCode" in names)
    }
}
