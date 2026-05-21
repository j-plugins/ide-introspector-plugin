package com.github.xepozz.introspectorplugin.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicReference
import javax.swing.SwingUtilities

/**
 * Runs [block] on the EDT and waits for the result up to [timeoutMs] ms.
 *
 * The MCP tool handlers run on a background ktor coroutine, so callers must wrap any Swing
 * access in this helper. Uses [ModalityState.any] so the block is not held back by an open
 * modal dialog (e.g. the exec-confirmation dialog from this same plugin).
 *
 * The timeout has to cover both queueing latency *and* the block's own runtime — make sure
 * [block] is fast (no deep tree walks with reflective property collection).
 */
fun <T> onEdtBlocking(timeoutMs: Long = DEFAULT_EDT_TIMEOUT_MS, block: () -> T): T {
    if (SwingUtilities.isEventDispatchThread()) {
        return block()
    }
    val app = ApplicationManager.getApplication()
    val ref = AtomicReference<Result<T>>()
    val latch = java.util.concurrent.CountDownLatch(1)
    app.invokeLater({
        ref.set(runCatching(block))
        latch.countDown()
    }, ModalityState.any())
    if (!latch.await(timeoutMs, TimeUnit.MILLISECONDS)) {
        throw TimeoutException("EDT operation timed out after ${timeoutMs}ms. " +
            "The EDT was busy or the block is too heavy — consider narrower rootSelector / lower maxDepth.")
    }
    return ref.get().getOrThrow()
}

/** Hard cap: any introspection that needs more than this is doing the wrong thing
 *  (narrow the rootSelector, drop includeProperties, etc.). */
const val DEFAULT_EDT_TIMEOUT_MS = 10_000L
