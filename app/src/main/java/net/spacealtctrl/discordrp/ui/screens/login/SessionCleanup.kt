package net.spacealtctrl.discordrp.ui.screens.login

import java.util.concurrent.atomic.AtomicBoolean

/**
 * The operations a login WebView needs for teardown, split out so the ordering
 * can be exercised without an Android runtime.
 */
internal interface SessionOps {
    fun stopPage()

    fun loadBlank()

    fun clearStorage()

    /** Must call [onDone] only after the cookie store is actually gone. */
    fun removeCookies(onDone: () -> Unit)

    fun flushCookies()

    fun clearCache()

    fun destroyPage()
}

/**
 * Tears a login WebView down in an order that cannot leak the document back:
 * stop loading, replace the document with about:blank, wait until that
 * navigation has finished (the old document is gone once it commits), wipe
 * storage, and only after [SessionOps.removeCookies] reports completion flush
 * the cookie store and consider the session cleaned.
 *
 * Both the successful capture path and the cancel path go through the same
 * instance, so the work happens once. Destruction is deferred until cleanup is
 * complete even when the composable is released first.
 */
internal class SessionCleanup(
    private val ops: SessionOps,
    private val scheduleTimeout: (action: () -> Unit) -> Unit,
) {
    private val started = AtomicBoolean(false)
    private var clearing = false
    private var complete = false
    private var destroyWhenComplete = false
    private var destroyed = false
    private var onCleaned: (() -> Unit)? = null

    fun start(onCleaned: (() -> Unit)? = null) {
        if (onCleaned != null) this.onCleaned = onCleaned
        if (complete) {
            deliver()
            return
        }
        if (!started.compareAndSet(false, true)) return
        ops.stopPage()
        ops.loadBlank()
        scheduleTimeout { clear() }
    }

    /** Called for every page finish; only the blank document starts cleanup. */
    fun onPageFinished(url: String) {
        if (url == BLANK_URL) clear()
    }

    fun release() {
        destroyWhenComplete = true
        if (complete) {
            destroy()
            return
        }
        start()
    }

    private fun clear() {
        if (clearing || complete) return
        clearing = true
        ops.clearStorage()
        ops.removeCookies {
            if (complete) return@removeCookies
            ops.flushCookies()
            complete = true
            ops.clearCache()
            deliver()
            if (destroyWhenComplete) destroy()
        }
    }

    private fun deliver() {
        val callback = onCleaned
        onCleaned = null
        callback?.invoke()
    }

    private fun destroy() {
        if (destroyed) return
        destroyed = true
        ops.destroyPage()
    }

    companion object {
        const val BLANK_URL = "about:blank"
    }
}
