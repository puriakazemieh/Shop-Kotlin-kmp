package com.kazemieh.network.common

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.*

actual fun httpClientEngine(): HttpClientEngineFactory<*> {
    return CIO
}
