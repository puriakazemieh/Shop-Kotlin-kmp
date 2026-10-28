package com.kazemieh.data.local

import com.kazemieh.config.capabilities.BackendKind
import com.kazemieh.config.capabilities.PrivateSessionNamespace
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TokenSettingsSecurityTest {
    @Test
    fun tokensDoNotSurviveANewJvmTokenSettingsInstance() {
        val namespace = PrivateSessionNamespace(
            backendKind = BackendKind.WORDPRESS,
            tenantId = "jvm-token-security-test",
            origin = "https://example.test"
        )
        val first = TokenManager(createTokenSettings(createSettings()), namespace)
        first.clearTokens()

        try {
            first.saveTokens("synthetic-access", "synthetic-refresh")
            assertEquals("synthetic-access", first.getAccessToken())
            assertEquals("synthetic-refresh", first.getRefreshToken())

            val afterRestart = TokenManager(createTokenSettings(createSettings()), namespace)
            assertNull(afterRestart.getAccessToken())
            assertNull(afterRestart.getRefreshToken())
        } finally {
            first.clearTokens()
        }
    }
}
