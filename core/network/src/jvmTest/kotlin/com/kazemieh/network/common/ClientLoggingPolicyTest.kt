package com.kazemieh.network.common

import io.ktor.client.plugins.logging.LogLevel
import kotlin.test.Test
import kotlin.test.assertEquals

class ClientLoggingPolicyTest {
    @Test
    fun `release HTTP logging is disabled so headers and bodies cannot be emitted`() {
        assertEquals(LogLevel.NONE, clientHttpLogLevel)
    }
}
