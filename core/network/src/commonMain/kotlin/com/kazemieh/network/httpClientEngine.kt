package com.kazemieh.network

import io.ktor.client.engine.HttpClientEngineFactory

expect fun httpClientEngine(): HttpClientEngineFactory<*>