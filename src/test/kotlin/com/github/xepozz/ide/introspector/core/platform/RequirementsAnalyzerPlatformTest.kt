package com.github.xepozz.ide.introspector.core.platform

import com.github.xepozz.ide.introspector.core.RequirementsAnalyzer
import com.github.xepozz.ide.introspector.model.CheckRequirementsResponse
import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Platform-level tests for [RequirementsAnalyzer]. Each fixture covers one branch of the
 * status decision tree from `docs/plans/arch-devkit-mirror.md`:
 *
 *  - **A** — Java `@RequiresReadLock` + bare caller → `mismatch`.
 *  - **A2** — Java `@RequiresReadLock` + caller inside `ReadAction.compute { svc.load() }` → `ok`.
 *  - **A3** — Java `@RequiresReadLock` + caller annotated `@RequiresReadLock` → `ok`.
 *  - **B** — Java `@RequiresEdt` + caller without → `mismatch`.
 *  - **C** — interface `@RequiresReadLock` + `includeImplementations=true` → callers of the
 *           impl are checked against the interface contract.
 *  - **D** — `@RequiresReadLockAbsence` + caller inside `ReadAction.nonBlocking { tgt() }`
 *           → `mismatch`. Regression for the v1 wrapper-set drift bug.
 *  - **E** — target with no annotation → `expected=[]`, no analysis.
 *  - **F** — unknown FQN → `TargetNotFound`.
 *  - **G** — lambda passed to an opaque dispatcher (`Executors.newSingleThreadExecutor().submit { tgt() }`)
 *           → `unknown`.
 *
 * Annotations + wrapper helpers are stubbed in the fixture project (the IntelliJ platform's
 * real ones aren't always on the [BasePlatformTestCase] mock-JDK classpath in a form PSI
 * can resolve to a `qualifiedName`). Every test packs target + callers into ONE file via
 * `configureByText` so `ReferencesSearch` against the in-memory PSI returns deterministic
 * results without depending on cross-file indexing.
 */
class RequirementsAnalyzerPlatformTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        addStubAnnotation("RequiresReadLock")
        addStubAnnotation("RequiresWriteLock")
        addStubAnnotation("RequiresReadLockAbsence")
        addStubAnnotation("RequiresEdt")
        addStubAnnotation("RequiresBackgroundThread")
        addStubAnnotation("RequiresBlockingContext")
        addReadActionStub()
        addApplicationManagerStub()
    }

    private fun addStubAnnotation(simpleName: String) {
        myFixture.addFileToProject(
            "com/intellij/util/concurrency/annotations/$simpleName.java",
            """
            package com.intellij.util.concurrency.annotations;
            import java.lang.annotation.*;
            @Retention(RetentionPolicy.CLASS)
            @Target({ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE})
            public @interface $simpleName {}
            """.trimIndent(),
        )
    }

    private fun addReadActionStub() {
        myFixture.addFileToProject(
            "com/intellij/openapi/application/ReadAction.java",
            """
            package com.intellij.openapi.application;
            import java.util.concurrent.Callable;
            public class ReadAction {
                public static void run(Runnable r) { r.run(); }
                public static <T> T compute(Callable<T> c) {
                    try { return c.call(); } catch (Exception e) { throw new RuntimeException(e); }
                }
                public static <T> NonBlocking<T> nonBlocking(Callable<T> c) { return new NonBlocking<T>(); }
                public static class NonBlocking<T> {
                    public T executeSynchronously() { return null; }
                }
            }
            """.trimIndent(),
        )
    }

    private fun addApplicationManagerStub() {
        myFixture.addFileToProject(
            "com/intellij/openapi/application/Application.java",
            """
            package com.intellij.openapi.application;
            public interface Application {
                void invokeLater(Runnable r);
                void invokeAndWait(Runnable r);
                java.util.concurrent.Future<?> executeOnPooledThread(Runnable r);
            }
            """.trimIndent(),
        )
        myFixture.addFileToProject(
            "com/intellij/openapi/application/ApplicationManager.java",
            """
            package com.intellij.openapi.application;
            public class ApplicationManager {
                public static Application getApplication() { return null; }
            }
            """.trimIndent(),
        )
    }

    private fun <T> read(block: () -> T): T {
        val ref = arrayOfNulls<Any>(1)
        ApplicationManager.getApplication().runReadAction {
            @Suppress("UNCHECKED_CAST")
            ref[0] = block() as Any?
        }
        @Suppress("UNCHECKED_CAST")
        return ref[0] as T
    }

    private fun analyzeByFqn(
        target: String,
        kind: RequirementsAnalyzer.WrapperKind,
        includeImplementations: Boolean = true,
    ): CheckRequirementsResponse = read {
        RequirementsAnalyzer.analyze(
            project = project,
            annotationFqns = when (kind) {
                RequirementsAnalyzer.WrapperKind.LOCK -> RequirementsAnalyzer.LOCK_ANNOTATION_FQNS
                RequirementsAnalyzer.WrapperKind.THREADING -> RequirementsAnalyzer.THREADING_ANNOTATION_FQNS
            },
            wrapperKind = kind,
            target = target,
            psiFile = null,
            offset = null,
            scopeKind = "project",
            includeImplementations = includeImplementations,
            maxCallSites = 100,
        )
    }

    // ============================================================================
    // A — Java @RequiresReadLock — bare caller → mismatch
    // ============================================================================

    fun testReadLockBareCallerIsMismatch() {
        myFixture.configureByText(
            "Sample.java",
            """
            package com.acme;
            import com.intellij.util.concurrency.annotations.RequiresReadLock;
            class Service {
                @RequiresReadLock
                public void load() {}
            }
            class Bare {
                public void caller(Service s) { s.load(); }
            }
            """.trimIndent(),
        )

        val response = analyzeByFqn("com.acme.Service.load", RequirementsAnalyzer.WrapperKind.LOCK)
        assertEquals(
            "expected one @RequiresReadLock in expected[]; got ${response.expected}",
            1,
            response.expected.size,
        )
        assertFalse("expected at least one call site", response.callSites.isEmpty())
        val site = response.callSites.first { it.callerSignature.contains("Bare") }
        assertEquals(
            "bare caller of @RequiresReadLock method must be 'mismatch' (got ${site.status}: ${site.reason})",
            "mismatch",
            site.status,
        )
    }

    // ============================================================================
    // A2 — caller inside ReadAction.compute → ok
    // ============================================================================

    fun testReadLockCallerInsideReadActionComputeIsOk() {
        myFixture.configureByText(
            "Sample.java",
            """
            package com.acme;
            import com.intellij.util.concurrency.annotations.RequiresReadLock;
            import com.intellij.openapi.application.ReadAction;
            class Service {
                @RequiresReadLock
                public void load() {}
            }
            class Good {
                public void caller(final Service s) {
                    ReadAction.compute(() -> { s.load(); return null; });
                }
            }
            """.trimIndent(),
        )

        val response = analyzeByFqn("com.acme.Service.load", RequirementsAnalyzer.WrapperKind.LOCK)
        val site = response.callSites.firstOrNull { it.callerSignature.contains("Good") }
        assertNotNull(
            "expected a call site in Good; got ${response.callSites.map { it.callerSignature }}",
            site,
        )
        assertEquals(
            "caller inside ReadAction.compute must be 'ok' (got ${site!!.status}: ${site.reason}; hints=${site.contextHints})",
            "ok",
            site.status,
        )
        assertTrue(
            "expected contextHints to include 'inside-compute', got ${site.contextHints}",
            site.contextHints.any { it == "inside-compute" },
        )
    }

    // ============================================================================
    // A3 — caller carries the same annotation → ok
    // ============================================================================

    fun testReadLockCallerAnnotatedSameIsOk() {
        myFixture.configureByText(
            "Sample.java",
            """
            package com.acme;
            import com.intellij.util.concurrency.annotations.RequiresReadLock;
            class Service {
                @RequiresReadLock
                public void load() {}
            }
            class Annotated {
                @RequiresReadLock
                public void caller(Service s) { s.load(); }
            }
            """.trimIndent(),
        )

        val response = analyzeByFqn("com.acme.Service.load", RequirementsAnalyzer.WrapperKind.LOCK)
        val site = response.callSites.firstOrNull { it.callerSignature.contains("Annotated") }
        assertNotNull(
            "expected a call site in Annotated; got ${response.callSites.map { it.callerSignature }}",
            site,
        )
        assertEquals(
            "caller carrying the same annotation must be 'ok' (got ${site!!.status}: ${site.reason})",
            "ok",
            site.status,
        )
    }

    // ============================================================================
    // B — @RequiresEdt with bare caller → mismatch
    // ============================================================================

    fun testEdtBareCallerIsMismatch() {
        myFixture.configureByText(
            "Sample.java",
            """
            package com.acme;
            import com.intellij.util.concurrency.annotations.RequiresEdt;
            class Ui {
                @RequiresEdt
                public void show() {}
            }
            class BareEdt {
                public void caller(Ui u) { u.show(); }
            }
            """.trimIndent(),
        )

        val response = analyzeByFqn("com.acme.Ui.show", RequirementsAnalyzer.WrapperKind.THREADING)
        assertEquals(
            "expected one @RequiresEdt in expected[]; got ${response.expected}",
            1,
            response.expected.size,
        )
        val site = response.callSites.firstOrNull { it.callerSignature.contains("BareEdt") }
        assertNotNull(
            "expected a call site in BareEdt; got ${response.callSites.map { it.callerSignature }}",
            site,
        )
        assertEquals(
            "bare caller of @RequiresEdt method must be 'mismatch' (got ${site!!.status}: ${site.reason})",
            "mismatch",
            site.status,
        )
    }

    // ============================================================================
    // C — interface + override (includeImplementations=true)
    // ============================================================================

    fun testIncludeImplementationsChecksCallersAgainstInterfaceContract() {
        myFixture.configureByText(
            "Sample.java",
            """
            package com.acme;
            import com.intellij.util.concurrency.annotations.RequiresReadLock;
            interface Repo {
                @RequiresReadLock
                Object all();
            }
            class RepoImpl implements Repo {
                public Object all() { return null; }
            }
            class BareImplCaller {
                public Object caller(RepoImpl impl) { return impl.all(); }
            }
            """.trimIndent(),
        )

        val response = analyzeByFqn(
            target = "com.acme.Repo.all",
            kind = RequirementsAnalyzer.WrapperKind.LOCK,
            includeImplementations = true,
        )
        val implSite = response.callSites.firstOrNull { it.callerSignature.contains("BareImplCaller") }
        assertNotNull(
            "expected a call site in BareImplCaller when includeImplementations=true; " +
                "got ${response.callSites.map { it.callerSignature }}",
            implSite,
        )
        assertEquals(
            "bare caller of RepoImpl.all() must inherit Repo.all()'s @RequiresReadLock contract → mismatch",
            "mismatch",
            implSite!!.status,
        )
    }

    // ============================================================================
    // D — @RequiresReadLockAbsence + ReadAction.nonBlocking REGRESSION TEST
    // ============================================================================

    fun testReadLockAbsenceInsideNonBlockingIsMismatch() {
        myFixture.configureByText(
            "Sample.java",
            """
            package com.acme;
            import com.intellij.util.concurrency.annotations.RequiresReadLockAbsence;
            import com.intellij.openapi.application.ReadAction;
            class HotPath {
                @RequiresReadLockAbsence
                public void exec() {}
            }
            class NonBlockingCaller {
                public void caller(final HotPath h) {
                    ReadAction.nonBlocking(() -> { h.exec(); return null; }).executeSynchronously();
                }
            }
            """.trimIndent(),
        )

        val response = analyzeByFqn("com.acme.HotPath.exec", RequirementsAnalyzer.WrapperKind.LOCK)
        val site = response.callSites.firstOrNull { it.callerSignature.contains("NonBlockingCaller") }
        assertNotNull(
            "expected a call site in NonBlockingCaller; got ${response.callSites.map { it.callerSignature }}",
            site,
        )
        // Regression: v1 advertised `nonBlocking` in LOCK_WRAPPER_SIMPLE but `isReadOrWriteActionWrapper`
        // matched only `inside-run*`/`inside-runReadAction`/`inside-runWriteAction`, so
        // `@RequiresReadLockAbsence` violations slipped through as 'ok'. The unified
        // WRAPPER_TABLE maps `nonBlocking` → READ_ACTION role; the absence path checks the
        // role set; → 'mismatch'.
        assertEquals(
            "caller inside ReadAction.nonBlocking violates @RequiresReadLockAbsence — must be 'mismatch' " +
                "(got ${site!!.status}: ${site.reason}; hints=${site.contextHints})",
            "mismatch",
            site.status,
        )
        assertTrue(
            "expected contextHints to include 'inside-nonBlocking', got ${site.contextHints}",
            site.contextHints.any { it == "inside-nonBlocking" },
        )
    }

    // ============================================================================
    // E — no annotation on target → expected=[], no analysis
    // ============================================================================

    fun testTargetWithoutAnnotationProducesEmptyExpected() {
        myFixture.configureByText(
            "Sample.java",
            """
            package com.acme;
            class Plain {
                public void doStuff() {}
            }
            class PlainCaller {
                public void caller(Plain p) { p.doStuff(); }
            }
            """.trimIndent(),
        )

        val response = analyzeByFqn("com.acme.Plain.doStuff", RequirementsAnalyzer.WrapperKind.LOCK)
        assertTrue(
            "expected = [] for target with no annotation; got ${response.expected}",
            response.expected.isEmpty(),
        )
        assertTrue(
            "callSites = [] when expected is empty; got ${response.callSites.size}",
            response.callSites.isEmpty(),
        )
        assertEquals(0, response.total)
        assertFalse("truncated must be false on empty response", response.truncated)
    }

    // ============================================================================
    // F — unknown FQN → TargetNotFound
    // ============================================================================

    fun testUnknownFqnThrowsTargetNotFound() {
        try {
            analyzeByFqn("com.nowhere.NoSuchClass.nope", RequirementsAnalyzer.WrapperKind.LOCK)
            fail("expected TargetNotFound for unknown class FQN")
        } catch (e: RequirementsAnalyzer.TargetNotFound) {
            assertTrue(
                "TargetNotFound message must reference the class FQN; got '${e.message}'",
                e.message?.contains("com.nowhere.NoSuchClass") == true,
            )
        }
    }

    // ============================================================================
    // G — lambda inside an opaque dispatcher → unknown
    // ============================================================================

    fun testCallerInsideOpaqueExecutorIsUnknown() {
        myFixture.configureByText(
            "Sample.java",
            """
            package com.acme;
            import com.intellij.util.concurrency.annotations.RequiresReadLock;
            import java.util.concurrent.Executors;
            class Heavy {
                @RequiresReadLock
                public void crunch() {}
            }
            class OpaqueCaller {
                public void caller(final Heavy h) {
                    Executors.newSingleThreadExecutor().submit(() -> { h.crunch(); });
                }
            }
            """.trimIndent(),
        )

        val response = analyzeByFqn("com.acme.Heavy.crunch", RequirementsAnalyzer.WrapperKind.LOCK)
        val site = response.callSites.firstOrNull { it.callerSignature.contains("OpaqueCaller") || it.callerSignature.contains("lambda") }
        assertNotNull(
            "expected a call site in OpaqueCaller; got ${response.callSites.map { it.callerSignature }}",
            site,
        )
        // Lambda passed to an unrecognised consumer (Executor.submit) → opaque dispatcher.
        // We surface 'unknown' so the agent escalates rather than reports a false 'mismatch'.
        assertEquals(
            "lambda inside opaque dispatcher must be 'unknown' (got ${site!!.status}: ${site.reason})",
            "unknown",
            site.status,
        )
    }
}
