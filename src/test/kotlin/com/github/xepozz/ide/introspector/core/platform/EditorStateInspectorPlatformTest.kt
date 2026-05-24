package com.github.xepozz.ide.introspector.core.platform

import com.github.xepozz.ide.introspector.core.EditorStateInspector
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Platform-level tests for [EditorStateInspector] — real `EditorImpl`, real document,
 * real folding model, real daemon for the gutter-marker tests.
 *
 * Pattern matches `PsiStructureWalkerPlatformTest`: each test calls
 * `myFixture.configureByText(...)` (which opens the file in the editor) and then drives
 * the inspector through its EDT-only entry points. The inspector itself assumes the
 * caller is already on the EDT; `BasePlatformTestCase` test methods run on the EDT.
 *
 * Covers (per `docs/plans/editor-group.md` test plan):
 *  1. setCaret happy path — `(line, column)` round-trips with the offset.
 *  2. setCaret with column past EOL — `line`/`column`/`newOffset` agree post-clamp,
 *     `clamped=true`. Regression test for the line/column-vs-offset disagreement bug.
 *  3. captureState selection capture — `selection.length` matches `end - start`.
 *  4. captureState folding capture — a `runBatchFoldingOperation` shows up in
 *     `foldedRanges` with `expanded=false`.
 *  5. captureState gutter markers via daemon — a fixture with a Java error triggers
 *     `doHighlighting` and at least one ERROR-severity marker shows up in
 *     `gutterMarkers`. Regression test for the wrong-markup-model fallback bug
 *     (when the daemon path fails, we now read the document markup model — which is
 *     what the daemon actually publishes to).
 */
class EditorStateInspectorPlatformTest : BasePlatformTestCase() {

    private fun <T> read(block: () -> T): T {
        val ref = arrayOfNulls<Any>(1)
        ApplicationManager.getApplication().runReadAction {
            @Suppress("UNCHECKED_CAST")
            ref[0] = block() as Any?
        }
        @Suppress("UNCHECKED_CAST")
        return ref[0] as T
    }

    private val editor: Editor get() = myFixture.editor

    // ============================================================================
    // setCaret happy path
    // ============================================================================

    fun testSetCaretHappyPath() {
        // 3 lines; line 2 is "second line" (offsets 6..17, line index 1).
        myFixture.configureByText("Sample.txt", "first\nsecond line\nthird\n")
        val response = EditorStateInspector.setCaretAtLineColumn(
            editor = editor,
            line = 2,
            column = 3,
            scrollToVisible = false,
        )
        assertEquals("ok", true, response.ok)
        assertEquals("clamped must be false on a valid line/col", false, response.clamped)
        // line=2, column=3 (1-based) -> logical (1, 2) -> offset 6 + 2 = 8
        assertEquals("line round-trip", 2, response.line)
        assertEquals("column round-trip", 3, response.column)
        assertEquals("offset matches lineStart(=6) + col-1(=2)", 8, response.newOffset)
        assertEquals("caret model offset matches newOffset", 8, editor.caretModel.offset)
    }

    // ============================================================================
    // setCaret with column past EOL — line/column/offset must agree post-clamp
    // Regression test for HIGH finding 1 in docs/reviews/editor-group.md.
    // ============================================================================

    fun testSetCaretClampsColumnAndKeepsFieldsConsistent() {
        // "0123456789" — 10 chars on the only logical line. Column=999 must clamp to col 11
        // (line length + 1, 1-based) and newOffset must be the line end (= 10).
        myFixture.configureByText("OneLine.txt", "0123456789")
        val response = EditorStateInspector.setCaretAtLineColumn(
            editor = editor,
            line = 1,
            column = 999,
            scrollToVisible = false,
        )
        assertEquals("clamped must be true when column overshoots line length", true, response.clamped)
        // newOffset is clamped to lineEnd (= 10 for a 10-char single line).
        assertEquals("newOffset clamps to line end", 10, response.newOffset)
        // CRITICAL: the post-move logical position MUST agree with newOffset.
        // Before the fix, column would come back as 999 (virtual-space) while newOffset = 10.
        assertEquals(
            "caret offset must equal newOffset (line/col/offset all tell the same story)",
            response.newOffset, editor.caretModel.offset,
        )
        // line/column reported in the response come from caretModel.logicalPosition AFTER the
        // move. For a 10-char line, the line-end position is logical (0, 10) → 1-based (1, 11).
        assertEquals("line must reflect the actual caret line", 1, response.line)
        assertEquals(
            "column must be the clamped column — NOT the virtual-space requested value",
            11, response.column,
        )
        // Sanity: line/column reported are derivable from newOffset.
        val pos = editor.caretModel.logicalPosition
        assertEquals("response.line matches logicalPosition.line + 1", pos.line + 1, response.line)
        assertEquals("response.column matches logicalPosition.column + 1", pos.column + 1, response.column)
    }

    // ============================================================================
    // captureState — selection
    // ============================================================================

    fun testCaptureStateSelectionMatchesSelectionModel() {
        myFixture.configureByText("Sel.txt", "hello world\nsecond\n")
        // Select "hello" (0..5).
        ApplicationManager.getApplication().invokeAndWait {
            editor.selectionModel.setSelection(0, 5)
        }
        val state = EditorStateInspector.captureState(
            project = myFixture.project,
            editor = editor,
            includeMultipleCarets = true,
            includeFolding = false,
            includeInlays = false,
            gutterMinSeverityName = "ERROR",
        )
        val selection = state.selection
        assertNotNull("selection must be non-null when text is selected", selection)
        assertEquals("selection start", 0, selection!!.start)
        assertEquals("selection end", 5, selection.end)
        assertEquals("selection length = end - start", 5, selection.length)
        assertEquals("selection both ends on line 1", 1, selection.startLine)
        assertEquals("selection both ends on line 1", 1, selection.endLine)
    }

    // ============================================================================
    // captureState — folding
    // ============================================================================

    fun testCaptureStateFoldingCapturesCollapsedRegion() {
        myFixture.configureByText("Fold.txt", "AAA\nBBB\nCCC\nDDD\n")
        // Add a collapsed fold region over "BBB\nCCC" (offsets 4..11).
        ApplicationManager.getApplication().invokeAndWait {
            editor.foldingModel.runBatchFoldingOperation {
                val region = editor.foldingModel.addFoldRegion(4, 11, "...")
                requireNotNull(region) { "addFoldRegion returned null" }
                region.isExpanded = false
            }
        }
        val state = EditorStateInspector.captureState(
            project = myFixture.project,
            editor = editor,
            includeMultipleCarets = false,
            includeFolding = true,
            includeInlays = false,
            gutterMinSeverityName = "ERROR",
        )
        val folds = state.foldedRanges
        assertNotNull("foldedRanges must be non-null when includeFolding=true", folds)
        val target = folds!!.firstOrNull { it.startOffset == 4 && it.endOffset == 11 }
        assertNotNull("expected our fold region in foldedRanges; got $folds", target)
        assertEquals("placeholder text", "...", target!!.placeholder)
        assertEquals("region must be collapsed", false, target.expanded)
    }

    // ============================================================================
    // captureState — gutter markers via daemon
    // Regression test for HIGH finding 2: when the DaemonCodeAnalyzerImpl path fails,
    // the fallback must read from the DOCUMENT markup model (where the daemon publishes),
    // not editor.markupModel (which holds per-editor highlighters like bookmarks).
    // ============================================================================

    fun testCaptureStateGutterMarkersFromDaemon() {
        // Java fixture with an unambiguous compile error: int x = "bad";
        myFixture.configureByText("Bug.java", "class Bug { void m() { int x = \"bad\"; } }")
        // Force the daemon to finish so highlights are published into the document markup model.
        myFixture.doHighlighting()

        val state = EditorStateInspector.captureState(
            project = myFixture.project,
            editor = editor,
            includeMultipleCarets = false,
            includeFolding = false,
            includeInlays = false,
            gutterMinSeverityName = "ERROR",
        )
        val markers = state.gutterMarkers
        assertNotNull("gutterMarkers must be non-null on success", markers)
        // Expect at least one ERROR-severity marker for the bad assignment.
        val errors = markers!!.filter { it.severity == HighlightSeverity.ERROR.name }
        assertEquals(
            "expected ≥1 ERROR marker for `int x = \"bad\";`; got $markers",
            true, errors.isNotEmpty(),
        )
    }
}
