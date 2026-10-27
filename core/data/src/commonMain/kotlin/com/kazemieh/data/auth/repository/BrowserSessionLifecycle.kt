package com.kazemieh.data.auth.repository

/**
 * Sends a credential-free browser event when a user signs out in another tab.
 * Native clients deliberately provide a no-op implementation.
 */
expect object BrowserSessionLifecycle {
    fun observeSignOut(listener: suspend () -> Unit)
    fun publishSignOut()
}

internal const val BrowserSignOutSignal = "carmilla-browser-sign-out-v1"

internal fun isBrowserSignOutSignal(message: Any?): Boolean =
    message == BrowserSignOutSignal
