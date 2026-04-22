package com.kazemieh.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.android.*

actual fun httpClientEngine(): HttpClientEngineFactory<*> {
    return Android
}