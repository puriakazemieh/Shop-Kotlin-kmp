package com.kazemieh.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.js.*

actual fun httpClientEngine(): HttpClientEngineFactory<*> {
    return Js
}