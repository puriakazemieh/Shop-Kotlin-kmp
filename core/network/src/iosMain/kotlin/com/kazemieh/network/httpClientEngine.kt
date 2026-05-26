package com.kazemieh.network.common

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.*

actual fun httpClientEngine(): HttpClientEngineFactory<*> {
    return Darwin
}
