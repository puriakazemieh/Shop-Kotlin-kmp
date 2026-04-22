package com.kazemieh.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.*

actual fun httpClientEngine(): HttpClientEngineFactory<*> {
    return Darwin
}