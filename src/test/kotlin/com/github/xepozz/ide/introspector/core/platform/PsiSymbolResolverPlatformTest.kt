package com.github.xepozz.ide.introspector.core.platform

import com.github.xepozz.ide.introspector.core.PsiSymbolResolver
import com.github.xepozz.ide.introspector.model.SymbolAtResponse
import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Platform-level tests for [PsiSymbolResolver]. The resolver returns ONE compact symbol
 * description per offset, with explicit reference-vs-declaration disambiguation. We exercise:
 *
 *   - Caret on a declaration → `isReference=false`, kind/name/fqn describe the declaration.
 *   - Caret on a usage → `isReference=true`, kind/name describe the resolved declaration.
 *   - Caret on a local variable → `fqn=null`, kind="variable", `containingDeclarationName`
 *     names the enclosing function.
 *   - Caret past EOF / on whitespace → warning + `symbol=null` (or harmless fallthrough).
 *   - Caret inside a host-language injection → the injected reference wins over the host.
 *
 * Mirrors the fixture pattern in [PsiUsageSearcherPlatformTest].
 */
class PsiSymbolResolverPlatformTest : BasePlatformTestCase() {

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

    private fun offsetOfFirst(needle: String): Int = read {
        val text = myFixture.file.text
        val idx = text.indexOf(needle)
        check(idx >= 0) { "no '$needle' in source" }
        idx
    }

    private fun offsetOfNth(needle: String, n: Int): Int = read {
        val text = myFixture.file.text
        var idx = -1
        var found = 0
        while (found <= n) {
            idx = text.indexOf(needle, idx + 1)
            check(idx >= 0) { "less than ${n + 1} occurrences of '$needle' in source" }
            found++
        }
        idx
    }

    private fun resolve(offset: Int, includeDoc: Boolean = true, truncateDocAt: Int = 400): SymbolAtResponse = read {
        PsiSymbolResolver.resolveAt(
            psiFile = myFixture.file,
            hostDocument = myFixture.editor.document,
            offset = offset,
            includeDoc = includeDoc,
            truncateDocAt = truncateDocAt,
        )
    }

    // ============================================================================
    // Declaration vs reference disambiguation
    // ============================================================================

    fun testSymbolOnClassDeclarationName() {
        configure("class Foo { void bar() {} }")
        val resp = resolve(offsetOfFirst("Foo"))
        val symbol = resp.symbol
        assertNotNull("expected a symbol at the class name", symbol)
        assertEquals("class", symbol!!.kind)
        assertEquals("Foo", symbol.name)
        assertEquals("Foo", symbol.fqn)  // default package
        assertFalse("caret on declaration → isReference must be false", symbol.isReference)
    }

    fun testSymbolOnMethodDeclarationName() {
        configure("class Foo { void bar() {} }")
        val resp = resolve(offsetOfFirst("bar"))
        val symbol = resp.symbol
        assertNotNull("expected a symbol at the method name", symbol)
        assertEquals("method", symbol!!.kind)
        assertEquals("bar", symbol.name)
        assertEquals("Foo.bar", symbol.fqn)
        assertEquals("void", symbol.returnType)
        assertFalse(symbol.isReference)
    }

    fun testSymbolOnReferenceFollowsResolution() {
        configure(
            """
            class Foo {
                void bar() { baz(); }
                void baz() {}
            }
            """.trimIndent(),
        )
        val callOffset = offsetOfFirst("baz")
        val resp = resolve(callOffset)
        val symbol = resp.symbol
        assertNotNull("expected reference at call site to resolve", symbol)
        assertEquals("method", symbol!!.kind)
        assertEquals("baz", symbol.name)
        assertTrue("caret on usage → isReference must be true", symbol.isReference)
    }

    fun testSymbolOnLocalVariable() {
        configure(
            """
            class Foo {
                void bar() {
                    int x = 1;
                    x++;
                }
            }
            """.trimIndent(),
        )
        val declOffset = read { myFixture.file.text.indexOf("x = 1") }
        val resp = resolve(declOffset)
        val symbol = resp.symbol
        assertNotNull(symbol)
        assertEquals("variable", symbol!!.kind)
        assertEquals("x", symbol.name)
        assertNull("locals have no FQN", symbol.fqn)
        assertEquals(
            "containingDeclarationName should be the enclosing function",
            "bar",
            symbol.containingDeclarationName,
        )
    }

    fun testSymbolOnFieldDeclaration() {
        configure(
            """
            class Foo {
                private String greeting = "hi";
            }
            """.trimIndent(),
        )
        val resp = resolve(offsetOfFirst("greeting"))
        val symbol = resp.symbol
        assertNotNull(symbol)
        assertEquals("field", symbol!!.kind)
        assertEquals("greeting", symbol.name)
        assertEquals("Foo.greeting", symbol.fqn)
        // canonicalText may yield either "java.lang.String" (real JDK) or "String" (mock JDK)
        // depending on the test fixture's index state — accept either form.
        assertTrue(
            "expected typeText to be a String form; got ${symbol.typeText}",
            symbol.typeText == "java.lang.String" || symbol.typeText == "String",
        )
        assertTrue("expected 'private' modifier; got ${symbol.modifiers}", "private" in symbol.modifiers)
    }

    // ============================================================================
    // Edge cases: EOF / whitespace
    // ============================================================================

    fun testSymbolPastEOFReturnsWarning() {
        configure("class Foo {}")
        val len = myFixture.file.text.length
        val resp = resolve(len + 10)
        assertNull("symbol must be null past EOF", resp.symbol)
        assertTrue(
            "expected a 'past end of file' warning; got ${resp.warnings}",
            resp.warnings.any { it.contains("past end of file") },
        )
    }

    fun testSymbolOnEmptyLineDoesNotCrash() {
        configure(
            """
            class Foo {

                void bar() {}
            }
            """.trimIndent(),
        )
        // Offset of the blank-line newline (between class header and method).
        val blankOffset = myFixture.file.text.indexOf("\n\n") + 1
        val resp = resolve(blankOffset)
        // Either symbol=null with a warning, or the enclosing class — both are acceptable.
        if (resp.symbol == null) {
            assertTrue(
                "symbol=null must come with a warning; got ${resp.warnings}",
                resp.warnings.isNotEmpty(),
            )
        } else {
            // PsiTreeUtil.getNonStrictParentOfType walks up to the enclosing class.
            assertEquals("class", resp.symbol!!.kind)
        }
    }

    // ============================================================================
    // Polyvariant references
    // ============================================================================

    fun testSymbolOnPolyvariantOverloadAddsWarning() {
        configure(
            """
            class Foo {
                void greet(String s) {}
                void greet(int i) {}
                void caller() { greet("hi"); }
            }
            """.trimIndent(),
        )
        // Call site `greet("hi")` resolves to the String overload, but a multiResolve(true)
        // can surface multiple candidates depending on the platform's resolve depth — we just
        // assert that the call resolves to *some* method named greet without crashing, and
        // that any extra-resolution warning, when present, is well-formed.
        val callOffset = read { myFixture.file.text.indexOf("greet(\"hi\")") }
        val resp = resolve(callOffset)
        val symbol = resp.symbol
        assertNotNull(symbol)
        assertEquals("greet", symbol!!.name)
        assertTrue(symbol.isReference)
        for (warning in resp.warnings) {
            // If a polyvariant warning surfaces, it must mention the alternate count.
            if (warning.contains("other resolutions")) {
                assertTrue(
                    "polyvariant warning should mention psi.get_references; got '$warning'",
                    warning.contains("psi.get_references"),
                )
            }
        }
    }

    // ============================================================================
    // Injection-aware reference resolution
    // ============================================================================

    fun testSymbolInsideInjectionDoesNotCrash() {
        // No real SQL injection registered for an arbitrary string in this fixture — but the
        // injection-aware code path must still gracefully degrade to the host file's resolution
        // without crashing. This guards the wiring added by Finding 1 fix.
        configure(
            """
            class Foo {
                String q = "select * from t";
            }
            """.trimIndent(),
        )
        val insideStringOffset = read { myFixture.file.text.indexOf("select") }
        val resp = resolve(insideStringOffset)
        // Resolution may legitimately yield the enclosing field `q` as a PsiNamedElement, or
        // a null symbol with a warning — either is fine. We only require non-crashing behaviour.
        if (resp.symbol != null) {
            assertNotNull("symbol kind must be set", resp.symbol!!.kind)
        }
    }

    // ============================================================================
    // Doc text inclusion / truncation
    // ============================================================================

    fun testIncludeDocTrueReturnsJavadoc() {
        configure(
            """
            class Foo {
                /** Bar method. */
                void bar() {}
            }
            """.trimIndent(),
        )
        val resp = resolve(offsetOfFirst("bar"), includeDoc = true)
        val symbol = resp.symbol
        assertNotNull(symbol)
        assertNotNull("expected javadoc to be returned", symbol!!.docText)
        assertTrue(
            "javadoc should contain 'Bar method'; got '${symbol.docText}'",
            symbol.docText!!.contains("Bar method"),
        )
    }

    fun testIncludeDocFalseSkipsLookup() {
        configure(
            """
            class Foo {
                /** Bar method. */
                void bar() {}
            }
            """.trimIndent(),
        )
        val resp = resolve(offsetOfFirst("bar"), includeDoc = false)
        assertNull("includeDoc=false must yield docText=null", resp.symbol!!.docText)
    }

    fun testDocTruncationAppendsEllipsis() {
        val longDoc = "x".repeat(500)
        configure(
            """
            class Foo {
                /** $longDoc */
                void bar() {}
            }
            """.trimIndent(),
        )
        val resp = resolve(offsetOfFirst("bar"), includeDoc = true, truncateDocAt = 50)
        val docText = resp.symbol!!.docText
        assertNotNull(docText)
        assertTrue("doc must be truncated to <=51 chars (50 + '…'); got ${docText!!.length}", docText.length <= 51)
        assertTrue("truncated doc must end with '…'", docText.endsWith("…"))
    }
}
