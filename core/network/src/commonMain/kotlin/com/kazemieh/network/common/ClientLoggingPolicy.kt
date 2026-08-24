package com.kazemieh.network.common

import io.ktor.client.plugins.logging.LogLevel

/** HTTP payloads and headers must never be written by the client logger. */
internal val clientHttpLogLevel = LogLevel.NONE
