package com.kazemieh.network

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.js.Js

actual fun httpClientEngine(): HttpClientEngineFactory<*> = Js
