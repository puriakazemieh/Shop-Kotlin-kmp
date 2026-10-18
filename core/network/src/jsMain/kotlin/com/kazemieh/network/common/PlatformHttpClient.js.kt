package com.kazemieh.network.common

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.js.Js

actual fun createPlatformHttpClient(
    block: HttpClientConfig<out HttpClientEngineConfig>.() -> Unit,
): HttpClient = HttpClient(Js) {
    engine {
        configureRequest {
            credentials = "include"
            // Fetch otherwise follows redirects internally, beyond Ktor's per-send guard.
            // Fail closed; backend API URLs must not require redirects in a browser.
            asDynamic().redirect = "error"
        }
    }
    block(this)
}
