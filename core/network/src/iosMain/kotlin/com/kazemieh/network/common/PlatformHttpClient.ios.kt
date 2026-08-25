package com.kazemieh.network.common

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig

actual fun createPlatformHttpClient(
    block: HttpClientConfig<out HttpClientEngineConfig>.() -> Unit,
): HttpClient = HttpClient(httpClientEngine()) { block(this) }
