package com.kazemieh.network.common

import kotlinx.browser.window

actual object PlatformConfig {
    actual val baseUrl = "http://localhost:8080/"
    actual val usesWebSessionCookie = true
    actual val isReleaseBuild: Boolean
        get() = runCatching {
            val host = window.location.hostname
            host.isNotBlank() && host != "localhost" && host != "127.0.0.1"
        }.getOrDefault(false)
}
