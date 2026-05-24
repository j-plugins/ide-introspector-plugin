package com.github.xepozz.ide.introspector.core.platform

import com.github.xepozz.ide.introspector.exec.UiActionConfirmationManager
import com.github.xepozz.ide.introspector.exec.UiActionSettings
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Locks in the corrected truth table for [UiActionConfirmationManager.confirm].
 *
 * The original implementation inverted the per-call `requireConfirmation` flag: any
 * caller passing `false` skipped stage 1 even when the user's global
 * `settings.requireConfirmation=true` (CVE-grade security bypass, see review Finding 1).
 * The fix mirrors the exec.* twin's `ConfirmationManager`:
 *
 *     stage1Needed = requireConfirmation OR (settings.requireConfirmation AND NOT sessionBypass)
 *
 * That is: `requireConfirmation=true` is a per-call FORCE-ON override, never an off
 * switch. Tests below exercise all four combinations × two `sessionBypass` states.
 *
 * Uses [BasePlatformTestCase] because [UiActionSettings] is an `@Service(APP)` — its
 * `getInstance()` requires a running `Application`. The dialog itself is stubbed via
 * [UiActionConfirmationManager.dialogFn]; no Swing window ever opens.
 */
class UiActionConfirmationManagerTest : BasePlatformTestCase() {

    private var prevEnabled = false
    private var prevConfirm = true
    private var prevAudit = true

    /** Calls the test seam recorded for assertion. */
    private val capturedRequests = mutableListOf<UiActionConfirmationManager.DialogRequest>()

    override fun setUp() {
        super.setUp()
        val s = UiActionSettings.getInstance()
        prevEnabled = s.enabled
        prevConfirm = s.requireConfirmation
        prevAudit = s.auditEnabled
        UiActionConfirmationManager.resetSessionBypassForTest()
        capturedRequests.clear()
    }

    override fun tearDown() {
        try {
            // Restore process-wide state — leaving the dialog stub installed would break
            // every later test that asks for a real confirmation.
            UiActionConfirmationManager.dialogFn = ::defaultRejecting
            UiActionConfirmationManager.resetSessionBypassForTest()
            val s = UiActionSettings.getInstance()
            s.enabled = prevEnabled
            s.requireConfirmation = prevConfirm
            s.auditEnabled = prevAudit
        } finally {
            super.tearDown()
        }
    }

    /**
     * Default fallback for the seam during tearDown — never approves, so any test that
     * forgets to install its own responder fails loudly instead of silently passing.
     */
    private fun defaultRejecting(req: UiActionConfirmationManager.DialogRequest):
        UiActionConfirmationManager.DialogResult =
            UiActionConfirmationManager.DialogResult(approved = false, bypassForSession = false)

    /**
     * Installs a responder that approves stage 1 (without bypassing the session) and
     * stage 2 alike. Captures every request the manager hands out for assertion.
     */
    private fun installApprovingDialog(bypassForSession: Boolean = false) {
        UiActionConfirmationManager.dialogFn = { req ->
            capturedRequests += req
            UiActionConfirmationManager.DialogResult(approved = true, bypassForSession = bypassForSession)
        }
    }

    /** Installs a responder that ALWAYS rejects — every captured request is recorded. */
    private fun installRejectingDialog() {
        UiActionConfirmationManager.dialogFn = { req ->
            capturedRequests += req
            UiActionConfirmationManager.DialogResult(approved = false, bypassForSession = false)
        }
    }

    private fun callConfirm(
        requireConfirmation: Boolean,
        isBlocklisted: Boolean = false,
    ): UiActionConfirmationManager.Decision = UiActionConfirmationManager.confirm(
        project = null,
        actionId = "Test.Action",
        actionText = "Test",
        pluginOwner = "test.plugin",
        component = null,
        componentId = "c_00000000",
        requireConfirmation = requireConfirmation,
        isBlocklisted = isBlocklisted,
    )

    // =============================================================================
    // Truth table: (requireConfirmation × settings.requireConfirmation × sessionBypass)
    // For non-blocklisted ids — stage 2 covered separately.
    // =============================================================================

    /**
     * (1) `requireConfirmation=true`, `settings.requireConfirmation=true`, no bypass.
     * Expected: stage 1 fires. Trivially required.
     */
    fun testStage1FiresWhenBothFlagsTrueAndNoBypass() {
        UiActionSettings.getInstance().requireConfirmation = true
        installApprovingDialog()

        val decision = callConfirm(requireConfirmation = true)

        assertEquals(UiActionConfirmationManager.Decision.Approved, decision)
        assertEquals("stage 1 must fire", 1, capturedRequests.size)
        assertEquals(1, capturedRequests.single().stage)
    }

    /**
     * (2) `requireConfirmation=false`, `settings.requireConfirmation=true`, no bypass.
     * Expected: stage 1 fires. This was the SECURITY BUG before fix — the implementation
     * required the per-call flag to be `true` as a precondition to prompting at all,
     * which let an agent pass `false` and skip the global setting.
     */
    fun testStage1FiresWhenGlobalSettingTrueEvenIfCallerFalse() {
        UiActionSettings.getInstance().requireConfirmation = true
        installApprovingDialog()

        val decision = callConfirm(requireConfirmation = false)

        assertEquals(UiActionConfirmationManager.Decision.Approved, decision)
        assertEquals(
            "stage 1 MUST fire when the global setting requires confirmation — " +
                "regression of the security bypass would skip it",
            1, capturedRequests.size,
        )
    }

    /**
     * (3) `requireConfirmation=true`, `settings.requireConfirmation=false`, no bypass.
     * Expected: stage 1 fires — the per-call flag is a FORCE-ON override and overrides
     * the user's "I disabled confirmation" preference. Documented at @McpDescription.
     */
    fun testStage1FiresWhenCallerForcesEvenIfGlobalDisabled() {
        UiActionSettings.getInstance().requireConfirmation = false
        installApprovingDialog()

        val decision = callConfirm(requireConfirmation = true)

        assertEquals(UiActionConfirmationManager.Decision.Approved, decision)
        assertEquals("force-on override must prompt regardless of settings", 1, capturedRequests.size)
    }

    /**
     * (4) `requireConfirmation=false`, `settings.requireConfirmation=false`, no bypass.
     * Expected: stage 1 SKIPPED — user has explicitly opted out of per-call prompts
     * and the caller does not force. Returns Approved without any dialog.
     */
    fun testStage1SkippedWhenBothFlagsFalse() {
        UiActionSettings.getInstance().requireConfirmation = false
        installRejectingDialog() // would auto-fail if dialog runs

        val decision = callConfirm(requireConfirmation = false)

        assertEquals(UiActionConfirmationManager.Decision.Approved, decision)
        assertTrue("stage 1 must NOT fire", capturedRequests.isEmpty())
    }

    // =============================================================================
    // Session-bypass interactions
    // =============================================================================

    /**
     * (5) session bypass active, `requireConfirmation=false`, global setting `true`.
     * Expected: stage 1 SKIPPED — bypass overrides the global setting. The caller
     * did not force.
     */
    fun testSessionBypassSkipsStage1WhenCallerDoesNotForce() {
        UiActionSettings.getInstance().requireConfirmation = true
        installApprovingDialog(bypassForSession = true)
        // First call: approve + tick the bypass so the manager flips its session flag.
        callConfirm(requireConfirmation = false)
        capturedRequests.clear()
        installRejectingDialog() // any further dialog == failure

        val decision = callConfirm(requireConfirmation = false)

        assertEquals(UiActionConfirmationManager.Decision.Approved, decision)
        assertTrue("session bypass must skip stage 1", capturedRequests.isEmpty())
    }

    /**
     * (6) session bypass active, `requireConfirmation=true`.
     * Expected: stage 1 FIRES — per-call force override beats the bypass. This is the
     * key safety property: an agent that genuinely wants the user to see the dialog
     * (e.g. a "confirm this destructive looking thing" check) can demand it.
     */
    fun testForceOnOverridesSessionBypass() {
        UiActionSettings.getInstance().requireConfirmation = true
        installApprovingDialog(bypassForSession = true)
        callConfirm(requireConfirmation = false)
        capturedRequests.clear()
        installApprovingDialog() // record but don't tick bypass

        val decision = callConfirm(requireConfirmation = true)

        assertEquals(UiActionConfirmationManager.Decision.Approved, decision)
        assertEquals(
            "force-on override must beat the session bypass",
            1, capturedRequests.size,
        )
    }

    // =============================================================================
    // Stage 2 (blocklist) is independent of stage 1 and the bypass state
    // =============================================================================

    /**
     * Blocklisted id under session bypass and caller `requireConfirmation=false`.
     * Stage 1 is correctly skipped; stage 2 MUST still fire — the blocklist gate
     * has no bypass.
     */
    fun testBlocklistStage2FiresEvenUnderBypass() {
        UiActionSettings.getInstance().requireConfirmation = true
        installApprovingDialog(bypassForSession = true)
        callConfirm(requireConfirmation = false)
        capturedRequests.clear()
        installApprovingDialog() // approve stage 2

        val decision = callConfirm(requireConfirmation = false, isBlocklisted = true)

        assertEquals(UiActionConfirmationManager.Decision.Approved, decision)
        assertEquals("only stage 2 should fire", 1, capturedRequests.size)
        assertEquals(2, capturedRequests.single().stage)
        assertFalse(
            "stage 2 must NOT offer a session bypass",
            capturedRequests.single().allowSessionBypass,
        )
    }

    /**
     * Stage 2 rejection returns the dedicated [Decision.RejectedBlocklist] enum value
     * (not a generic [Decision.Rejected]) so the toolset can emit the
     * `user-rejected-blocklist` audit outcome.
     */
    fun testBlocklistStage2RejectionReportsDedicatedDecision() {
        UiActionSettings.getInstance().requireConfirmation = false
        installRejectingDialog()

        val decision = callConfirm(requireConfirmation = false, isBlocklisted = true)

        assertEquals(UiActionConfirmationManager.Decision.RejectedBlocklist, decision)
    }

    /**
     * Stage 1 rejection short-circuits — stage 2 must NOT run even when blocklisted,
     * since the user has already said no.
     */
    fun testStage1RejectionSkipsStage2() {
        UiActionSettings.getInstance().requireConfirmation = true
        installRejectingDialog()

        val decision = callConfirm(requireConfirmation = true, isBlocklisted = true)

        assertEquals(UiActionConfirmationManager.Decision.Rejected, decision)
        assertEquals(
            "only stage 1 should have been shown — rejection short-circuits stage 2",
            1, capturedRequests.size,
        )
        assertEquals(1, capturedRequests.single().stage)
    }
}
