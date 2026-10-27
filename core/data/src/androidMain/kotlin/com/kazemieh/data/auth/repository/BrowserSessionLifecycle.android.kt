package com.kazemieh.data.auth.repository

actual object BrowserSessionLifecycle {
    actual fun observeSignOut(listener: suspend () -> Unit) = Unit
    actual fun publishSignOut() = Unit
}
