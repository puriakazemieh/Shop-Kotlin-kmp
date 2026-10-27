package com.kazemieh.data.auth.repository

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

actual object BrowserSessionLifecycle {
    private val scope = MainScope()
    private var listener: (suspend () -> Unit)? = null
    private val channel: dynamic = js(
        "typeof BroadcastChannel === 'undefined' ? null : new BroadcastChannel('carmilla-browser-session-v1')"
    )

    init {
        if (channel != null) {
            channel.onmessage = { event: dynamic ->
                if (isBrowserSignOutSignal(event.data)) {
                    listener?.let { callback -> scope.launch { callback() } }
                }
            }
        }
    }

    actual fun observeSignOut(listener: suspend () -> Unit) {
        this.listener = listener
    }

    actual fun publishSignOut() {
        channel?.postMessage(BrowserSignOutSignal)
    }
}
