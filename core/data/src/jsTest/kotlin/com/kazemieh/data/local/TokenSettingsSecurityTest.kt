package com.kazemieh.data.local

import com.kazemieh.config.capabilities.BackendKind
import com.kazemieh.config.capabilities.PrivateSessionNamespace
import kotlinx.browser.window
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TokenSettingsSecurityTest {
    @Test
    fun tokensStayInMemoryAndAreRemovedOnNamespaceChange() {
        val first = PrivateSessionNamespace(BackendKind.WORDPRESS, "qa-isolated-test", "http://localhost")
        val second = PrivateSessionNamespace(BackendKind.WORDPRESS, "qa-isolated-test-2", "http://localhost")
        val accessKey = "carmilla.settings.${first.key("access_token")}"
        val refreshKey = "carmilla.settings.${first.key("refresh_token")}"
        assertNull(window.localStorage.getItem(accessKey), "Test key must not already exist")
        assertNull(window.localStorage.getItem(refreshKey), "Test key must not already exist")
        val tokens = TokenManager(createTokenSettings(createSettings()), first)
        try {
            tokens.saveTokens("synthetic-access", "synthetic-refresh")
            assertEquals("synthetic-access", tokens.getAccessToken())
            assertNull(window.localStorage.getItem(accessKey))
            assertNull(window.localStorage.getItem(refreshKey))
            assertNull(window.sessionStorage.getItem(accessKey))
            assertNull(window.sessionStorage.getItem(refreshKey))
            tokens.switchNamespace(second)
            assertNull(tokens.getAccessToken())
            assertNull(tokens.getRefreshToken())
            tokens.switchNamespace(first)
            assertNull(tokens.getAccessToken())
            assertNull(tokens.getRefreshToken())
            assertNull(TokenManager(createTokenSettings(createSettings()), first).getAccessToken())
        } finally {
            window.localStorage.removeItem(accessKey)
            window.localStorage.removeItem(refreshKey)
        }
    }
}
