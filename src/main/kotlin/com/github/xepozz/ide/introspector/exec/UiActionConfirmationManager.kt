package com.github.xepozz.ide.introspector.exec

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.xml.util.XmlStringUtil
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.BorderFactory
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Two-stage per-call confirmation dialog for `ui.invoke_action_on`.
 *
 *  Stage 1 — normal prompt with an in-memory "don't ask again for this session" opt-out.
 *  Stage 2 — fires ONLY when the actionId hits [UiActionBlocklist]. ALWAYS shown
 *            regardless of session bypass / `requireConfirmation=false`. No opt-out
 *            available; the user must approve every single blocklisted invocation.
 *
 * Session bypass state is SEPARATE from [ConfirmationManager.sessionBypass] — exec and
 * action consent live in different threat models, and approving "run Kotlin without
 * confirmation" should not silently approve "invoke any non-blocklisted action without
 * confirmation".
 */
object UiActionConfirmationManager {

    @Volatile private var sessionBypass: Boolean = false

    /**
     * Test seam: lets unit tests substitute the showDialog implementation so the truth
     * table around [confirm] can be exercised headlessly. Default delegates to the real
     * Swing dialog; tests assign a deterministic responder and restore the default in
     * `tearDown` (see `UiActionConfirmationManagerTest`).
     */
    @Volatile internal var dialogFn: (DialogRequest) -> DialogResult = ::defaultShowDialog

    /** Test seam for the in-memory bypass flag. Only the test file should touch this. */
    internal fun resetSessionBypassForTest() { sessionBypass = false }

    internal data class DialogRequest(
        val project: Project?,
        val title: String,
        val stage: Int,
        val actionId: String,
        val actionText: String?,
        val pluginOwner: String?,
        val component: Component?,
        val componentId: String,
        val allowSessionBypass: Boolean,
    )

    internal data class DialogResult(val approved: Boolean, val bypassForSession: Boolean)

    /**
     * Returns true iff the user approves the call (both stages, when stage 2 is required).
     *
     *  @param project           Owning project for dialog parenting.
     *  @param actionId          The id being invoked.
     *  @param actionText        Resolved `Presentation.text` (may be null when action absent).
     *  @param pluginOwner       Plugin id that owns the action, for trust attribution.
     *  @param component         Target Swing component — described in the dialog body.
     *  @param componentId       Registry id of the target — shown for traceability.
     *  @param requireConfirmation
     *                           Per-call FORCE-ON override. When `true`, stage 1 always
     *                           fires (even past the session bypass). When `false`, the
     *                           global setting + session-bypass state decide whether to
     *                           prompt. Stage 2 (blocklist) always fires independently.
     *  @param isBlocklisted     Whether the actionId hits [UiActionBlocklist] (precomputed
     *                           by the caller — keeps blocklist resolution in one place).
     */
    fun confirm(
        project: Project?,
        actionId: String,
        actionText: String?,
        pluginOwner: String?,
        component: Component?,
        componentId: String,
        requireConfirmation: Boolean,
        isBlocklisted: Boolean,
    ): Decision {
        val settings = UiActionSettings.getInstance()

        // ----- Stage 1 -----
        // `requireConfirmation=true` is a per-call FORCE-ON override (matches the
        // documented @McpDescription + the exec.* twin's `ConfirmationManager`):
        // it MUST prompt even when the user has flipped the session bypass.
        // When the caller passes `false`, fall back to the global setting AND the
        // session-bypass state. Inverting this boolean was the security bug — an
        // agent could pass `requireConfirmation=false` and skip the per-call gate
        // even though the user's global `settings.requireConfirmation=true`.
        val stage1Needed = requireConfirmation
            || (settings.requireConfirmation && !sessionBypass)
        if (stage1Needed) {
            val stage1Result = dialogFn(
                DialogRequest(
                    project = project,
                    title = "Invoke IDE action",
                    stage = 1,
                    actionId = actionId,
                    actionText = actionText,
                    pluginOwner = pluginOwner,
                    component = component,
                    componentId = componentId,
                    allowSessionBypass = true,
                )
            )
            if (!stage1Result.approved) return Decision.Rejected
            if (stage1Result.bypassForSession) sessionBypass = true
        }

        // ----- Stage 2 (blocklist forced) -----
        if (isBlocklisted) {
            val stage2Result = dialogFn(
                DialogRequest(
                    project = project,
                    title = "Confirm potentially destructive action",
                    stage = 2,
                    actionId = actionId,
                    actionText = actionText,
                    pluginOwner = pluginOwner,
                    component = component,
                    componentId = componentId,
                    allowSessionBypass = false,
                )
            )
            if (!stage2Result.approved) return Decision.RejectedBlocklist
        }
        return Decision.Approved
    }

    enum class Decision { Approved, Rejected, RejectedBlocklist }

    private fun defaultShowDialog(req: DialogRequest): DialogResult {
        var approved = false
        var bypass = false
        ApplicationManager.getApplication().invokeAndWait {
            val dialog = Dialog(
                project = req.project,
                title = req.title,
                stage = req.stage,
                actionId = req.actionId,
                actionText = req.actionText,
                pluginOwner = req.pluginOwner,
                component = req.component,
                componentId = req.componentId,
                allowSessionBypass = req.allowSessionBypass,
            )
            dialog.show()
            approved = dialog.exitCode != DialogWrapper.CANCEL_EXIT_CODE
            bypass = dialog.bypassForSession
        }
        return DialogResult(approved, bypass)
    }

    private class Dialog(
        project: Project?,
        title: String,
        private val stage: Int,
        private val actionId: String,
        private val actionText: String?,
        private val pluginOwner: String?,
        private val component: Component?,
        private val componentId: String,
        private val allowSessionBypass: Boolean,
    ) : DialogWrapper(project, true) {

        var bypassForSession = false

        init {
            this.title = title
            setOKButtonText(if (stage == 2) "Invoke anyway" else "Invoke")
            setCancelButtonText("Reject")
            init()
        }

        override fun createCenterPanel(): JComponent {
            val panel = JPanel(BorderLayout(8, 8))
            panel.preferredSize = Dimension(560, 280)
            val header = if (stage == 2) {
                "This action id matches the destructive-action blocklist. " +
                    "Approve only if you really want it to run."
            } else {
                "An MCP client wants to invoke this IntelliJ action against a specific component:"
            }
            panel.add(JBLabel(header), BorderLayout.NORTH)

            val grid = JPanel(GridLayout(0, 2, 6, 4))
            grid.border = BorderFactory.createEmptyBorder(8, 8, 8, 8)
            // SECURITY: every agent-/plugin-controlled string below MUST flow through
            // [safeLabel]. JLabel / JBLabel auto-render HTML when the string starts with
            // `<html>`, which an attacker could exploit to spoof the confirmation
            // dialog's identity fields (e.g. actionId="<html><b>Trusted</b> Build").
            grid.add(JBLabel("Action id:")); grid.add(safeLabel(actionId))
            grid.add(JBLabel("Action text:")); grid.add(safeLabel(actionText ?: "(unresolved)"))
            grid.add(JBLabel("Owning plugin:")); grid.add(safeLabel(pluginOwner ?: "(unknown)"))
            grid.add(JBLabel("Target component:")); grid.add(safeLabel(describeComponent(component)))
            grid.add(JBLabel("Component id:")); grid.add(safeLabel(componentId))
            panel.add(grid, BorderLayout.CENTER)

            if (allowSessionBypass) {
                val bypass = JCheckBox("Invoke and don't ask again for this session")
                bypass.addActionListener { bypassForSession = bypass.isSelected }
                panel.add(bypass, BorderLayout.SOUTH)
            }
            return panel
        }

        private fun describeComponent(c: Component?): String {
            if (c == null) return "(detached)"
            val b = c.bounds
            return "${c.javaClass.simpleName} ${b.width}x${b.height}@${b.x},${b.y}"
        }

        /**
         * Renders an untrusted string as plain text in a JBLabel. Wraps in `<html>` after
         * escaping so JBLabel's HTML auto-detection treats the value as literal text —
         * the escape ensures `<`, `>`, `&`, quotes can never close out into markup.
         */
        private fun safeLabel(value: String): JBLabel =
            JBLabel("<html>${XmlStringUtil.escapeString(value)}</html>")
    }
}
