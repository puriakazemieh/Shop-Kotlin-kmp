package com.kazemieh.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.*

actual fun httpClientEngine(): HttpClientEngineFactory<*> {
    return CIO
}