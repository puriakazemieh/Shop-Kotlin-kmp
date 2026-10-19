package com.kazemieh.network.common

import com.sun.net.httpserver.HttpServer
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NativeRefreshContractTest {
    private class SyntheticTokens(var refresh: String?) : TokenProvider {
        var access: String? = "synthetic-old-access"
        var expired = 0
        override fun saveTokens(accessToken: String, refreshToken: String) { access = accessToken; refresh = refreshToken }
        override fun getAccessToken() = access
        override fun getRefreshToken() = refresh
        override fun clearTokens() { access = null; refresh = null }
        override fun setOnTokenExpiredListener(listener: () -> Unit) = Unit
        override fun notifyTokenExpired() { expired++ }
    }

    @Test
    fun `native refresh requires and sends refresh credential and accepts rotation`() = runBlocking {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        var refreshRequests = 0
        var sentSyntheticRefresh = false
        var omitRotatedRefresh = false
        server.createContext("/api/auth/refresh") { exchange ->
            refreshRequests++
            sentSyntheticRefresh = exchange.requestBody.bufferedReader().readText().contains("synthetic-refresh")
            val body = (if (omitRotatedRefresh) """{"accessToken":"synthetic-new-access"}""" else """{"accessToken":"synthetic-new-access","refreshToken":"synthetic-new-refresh"}""").toByteArray()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        server.createContext("/protected") { exchange ->
            if (exchange.requestHeaders.getFirst("Authorization") == "Bearer synthetic-new-access") {
                exchange.sendResponseHeaders(200, -1)
            } else {
                exchange.responseHeaders.add("WWW-Authenticate", "Bearer")
                exchange.sendResponseHeaders(401, -1)
            }
            exchange.close()
        }
        server.start()
        ApiConfig.baseUrlOverride = "http://127.0.0.1:${server.address.port}/"
        val missing = SyntheticTokens(null)
        val missingClient = HttpClientFactory.create(missing)
        val tokens = SyntheticTokens("synthetic-refresh")
        val client = HttpClientFactory.create(tokens)
        try {
            assertEquals(401, missingClient.get("/protected").status.value)
            assertEquals(0, refreshRequests)
            assertEquals(200, client.get("/protected").status.value)
            assertEquals(1, refreshRequests)
            assertTrue(sentSyntheticRefresh)
            assertEquals("synthetic-new-access", tokens.access)
            assertEquals("synthetic-new-refresh", tokens.refresh)
            assertEquals(0, tokens.expired)
            assertFalse(PlatformConfig.usesWebSessionCookie)
            omitRotatedRefresh = true
            val malformed = SyntheticTokens("synthetic-refresh")
            val malformedClient = HttpClientFactory.create(malformed)
            try {
                assertEquals(401, malformedClient.get("/protected").status.value)
                assertEquals(null, malformed.access)
                assertEquals(null, malformed.refresh)
                assertEquals(1, malformed.expired)
            } finally { malformedClient.close() }
        } finally {
            missingClient.close(); client.close(); server.stop(0); ApiConfig.baseUrlOverride = null
        }
    }
}
