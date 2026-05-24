package com.github.xepozz.ide.introspector.core.platform

import com.github.xepozz.ide.introspector.core.ClassCatalog
import com.github.xepozz.ide.introspector.model.ListClassesResponse
import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Platform-level tests for [ClassCatalog]. Exercises the public `listInModule` and
 * `listInPackage` entry points against synthetic Java + Kotlin fixtures provided by
 * [BasePlatformTestCase]. Covers:
 *  - module variant: production-only listing, `truncated` semantics, `moduleName="nope"`.
 *  - package variant: non-recursive vs recursive; `recursive=true` does NOT widen to
 *    library subpackages (regression for the `pkg.subPackages` no-scope bug).
 *  - shared: Kotlin file with top-level `fun foo()` surfaces as `kotlinFileFacade`.
 *
 * Each call is wrapped in `runReadAction { … }` because `JavaPsiFacade.findPackage`,
 * `PsiPackage.getClasses`, and `ProjectFileIndex.iterateContent` all require a read
 * action (matching how `CodeSourceToolset` wraps them under `readActionBlocking`).
 */
class ClassCatalogPlatformTest : BasePlatformTestCase() {

    private fun <T> read(block: () -> T): T {
        val ref = arrayOfNulls<Any>(1)
        ApplicationManager.getApplication().runReadAction {
            @Suppress("UNCHECKED_CAST")
            ref[0] = block() as Any?
        }
        @Suppress("UNCHECKED_CAST")
        return ref[0] as T
    }

    private fun listInModule(
        moduleName: String = module.name,
        packagePrefix: String? = null,
        includeTests: Boolean = false,
        includeGenerated: Boolean = true,
        kinds: Collection<String> = setOf(
            "class", "interface", "enum", "record", "annotation", "kotlinFileFacade",
        ),
        limit: Int = 1000,
    ): Pair<ListClassesResponse, ClassCatalog.ModuleLookup> = read {
        ClassCatalog.listInModule(
            project = project,
            moduleName = moduleName,
            packagePrefix = packagePrefix,
            includeTests = includeTests,
            includeGenerated = includeGenerated,
            kinds = kinds,
            limit = limit,
        )
    }

    private fun listInPackage(
        packageFqn: String,
        recursive: Boolean = false,
        includeLibraries: Boolean = false,
        kinds: Collection<String> = setOf(
            "class", "interface", "enum", "record", "annotation", "kotlinFileFacade",
        ),
        limit: Int = 500,
    ): ListClassesResponse = read {
        ClassCatalog.listInPackage(
            project = project,
            packageFqn = packageFqn,
            recursive = recursive,
            includeLibraries = includeLibraries,
            kinds = kinds,
            limit = limit,
        )
    }

    // ============================================================================
    // listInModule — module variant
    // ============================================================================

    fun testListInModuleReturnsTopLevelProductionClasses() {
        myFixture.addFileToProject(
            "src/com/acme/Foo.java",
            """
            package com.acme;
            public class Foo {
                static class Inner {}
            }
            """.trimIndent(),
        )
        myFixture.addFileToProject(
            "src/com/acme/Bar.java",
            """
            package com.acme;
            public interface Bar {}
            """.trimIndent(),
        )

        val (response, lookup) = listInModule()

        assertEquals(ClassCatalog.ModuleLookup.FOUND, lookup)
        val fqns = response.classes.map { it.fqn }.toSet()
        assertTrue("expected com.acme.Foo in $fqns", "com.acme.Foo" in fqns)
        assertTrue("expected com.acme.Bar in $fqns", "com.acme.Bar" in fqns)
        // Inner classes must NOT surface in v1 — top-level only via PsiClassOwner.classes.
        assertFalse("must not include inner class; got $fqns", "com.acme.Foo.Inner" in fqns)
        assertFalse("must not include nested-dollar form; got $fqns", "com.acme.Foo\$Inner" in fqns)

        val foo = response.classes.first { it.fqn == "com.acme.Foo" }
        assertEquals("com.acme", foo.pkg)
        assertEquals("Foo", foo.simpleName)
        assertEquals("class", foo.kind)
        val bar = response.classes.first { it.fqn == "com.acme.Bar" }
        assertEquals("interface", bar.kind)
    }

    fun testListInModuleNotFoundReturnsLookupNotFound() {
        val (response, lookup) = listInModule(moduleName = "definitely-not-a-real-module")

        assertEquals(ClassCatalog.ModuleLookup.NOT_FOUND, lookup)
        assertEquals(0, response.total)
        assertTrue("classes must be empty", response.classes.isEmpty())
        assertFalse(response.truncated)
        // scope echoes the requested moduleName.
        assertEquals("definitely-not-a-real-module", response.scope)
    }

    fun testListInModuleRespectsLimitAndMarksTruncated() {
        // Five top-level classes; ask for limit=2.
        for (i in 1..5) {
            myFixture.addFileToProject(
                "src/com/acme/Cls$i.java",
                """
                package com.acme;
                public class Cls$i {}
                """.trimIndent(),
            )
        }
        val (response, _) = listInModule(limit = 2)

        assertEquals(2, response.classes.size)
        assertTrue("expected total>=5, got ${response.total}", response.total >= 5)
        assertTrue("must be marked truncated when total>collected", response.truncated)
    }

    fun testListInModulePackagePrefixFilter() {
        myFixture.addFileToProject(
            "src/com/acme/billing/Invoice.java",
            "package com.acme.billing; public class Invoice {}",
        )
        myFixture.addFileToProject(
            "src/com/acme/payments/Refund.java",
            "package com.acme.payments; public class Refund {}",
        )

        val (response, _) = listInModule(packagePrefix = "com.acme.billing")
        val fqns = response.classes.map { it.fqn }.toSet()
        assertTrue("expected com.acme.billing.Invoice in $fqns", "com.acme.billing.Invoice" in fqns)
        assertFalse("must filter out com.acme.payments.Refund; got $fqns",
            "com.acme.payments.Refund" in fqns)
    }

    // ============================================================================
    // listInPackage — package variant
    // ============================================================================

    fun testListInPackageNonRecursiveReturnsDirectChildren() {
        // `findPackage(fqn)` resolves via directory layout — the file's containing dir must
        // match the FQN. `BasePlatformTestCase` puts the source root at the temp-dir root,
        // so paths use bare package directories (no `src/` prefix).
        myFixture.addFileToProject(
            "com/acme/Direct.java",
            "package com.acme; public class Direct {}",
        )
        myFixture.addFileToProject(
            "com/acme/sub/Deep.java",
            "package com.acme.sub; public class Deep {}",
        )

        val response = listInPackage("com.acme", recursive = false)
        val fqns = response.classes.map { it.fqn }.toSet()

        assertTrue("expected direct child com.acme.Direct in $fqns", "com.acme.Direct" in fqns)
        assertFalse("non-recursive must NOT include com.acme.sub.Deep; got $fqns",
            "com.acme.sub.Deep" in fqns)
    }

    fun testListInPackageRecursiveDescendsScopedSubPackages() {
        // Regression for Finding 1: `pkg.subPackages` (no-scope) was used previously, which
        // BFSed into JDK subpackages with `includeLibraries=false`. The scoped overload
        // `getSubPackages(scope)` keeps the walk inside project sources.
        myFixture.addFileToProject(
            "com/acme/Direct.java",
            "package com.acme; public class Direct {}",
        )
        myFixture.addFileToProject(
            "com/acme/sub/Deep.java",
            "package com.acme.sub; public class Deep {}",
        )
        myFixture.addFileToProject(
            "com/acme/sub/deeper/Deeper.java",
            "package com.acme.sub.deeper; public class Deeper {}",
        )

        val response = listInPackage("com.acme", recursive = true)
        val fqns = response.classes.map { it.fqn }.toSet()

        assertTrue("expected com.acme.Direct in $fqns", "com.acme.Direct" in fqns)
        assertTrue("recursive must find com.acme.sub.Deep in $fqns", "com.acme.sub.Deep" in fqns)
        assertTrue("recursive must find com.acme.sub.deeper.Deeper in $fqns",
            "com.acme.sub.deeper.Deeper" in fqns)
        // No JDK class should leak in with includeLibraries=false.
        assertFalse("must not include JDK class with includeLibraries=false; got $fqns",
            fqns.any { it.startsWith("java.") })
        assertFalse(response.timedOut)
    }

    fun testListInPackageMissingPackageReturnsEmptyNotError() {
        val response = listInPackage("com.does.not.exist")
        assertEquals(0, response.total)
        assertTrue(response.classes.isEmpty())
        assertEquals("com.does.not.exist", response.scope)
    }

    // ============================================================================
    // Shared — Kotlin file facade kind
    // ============================================================================

    fun testKotlinFileWithTopLevelFunctionYieldsKotlinFileFacade() {
        myFixture.addFileToProject(
            "src/com/acme/Helpers.kt",
            """
            package com.acme

            fun helper(): String = "hi"
            """.trimIndent(),
        )

        val (response, lookup) = listInModule()
        assertEquals(ClassCatalog.ModuleLookup.FOUND, lookup)

        // The Kotlin top-level fn synthesises a `HelpersKt` light class. Its kind must
        // be `kotlinFileFacade` so agents can tell it apart from a real `class HelpersKt`.
        val facade = response.classes.firstOrNull { it.simpleName == "HelpersKt" }
        assertNotNull("expected synthetic HelpersKt facade class; got " +
            "${response.classes.map { it.fqn }}", facade)
        assertEquals("kotlinFileFacade", facade!!.kind)
        assertEquals("com.acme", facade.pkg)
        assertEquals("com.acme.HelpersKt", facade.fqn)
    }
}
