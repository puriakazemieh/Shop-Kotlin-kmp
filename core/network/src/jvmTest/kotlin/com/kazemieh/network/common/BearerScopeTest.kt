package com.kazemieh.network.common

import com.sun.net.httpserver.HttpServer
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
import java.util.Collections
import kotlin.test.Test
import kotlin.test.assertEquals

class BearerScopeTest {
    private class SyntheticTokens : TokenProvider {
        override fun saveTokens(accessToken: String, refreshToken: String) = Unit
        override fun getAccessToken() = "synthetic-access"
        override fun getRefreshToken(): String? = null
        override fun clearTokens() = Unit
        override fun setOnTokenExpiredListener(listener: () -> Unit) = Unit
        override fun notifyTokenExpired() = Unit
    }

    @Test
    fun `bearer stays inside the configured origin and path through challenges and redirects`() = runBlocking {
        val seen = Collections.synchronizedList(mutableListOf<Pair<String, Boolean>>())
        val api = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val other = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        api.createContext("/") { exchange ->
            val path = exchange.requestURI.path
            seen.add("api:$path" to (exchange.requestHeaders.getFirst("Authorization") != null))
            if (path == "/base/redirect") exchange.responseHeaders.add("Location", "/outside")
            if (path == "/base/foreign-redirect") exchange.responseHeaders.add("Location", "http://127.0.0.1:${other.address.port}/resource")
            val code = if (path == "/base/redirect" || path == "/base/foreign-redirect") 302 else 200
            exchange.sendResponseHeaders(code, -1)
            exchange.close()
        }
        other.createContext("/") { exchange ->
            seen.add("other" to (exchange.requestHeaders.getFirst("Authorization") != null))
            exchange.responseHeaders.add("WWW-Authenticate", "Bearer")
            exchange.sendResponseHeaders(401, -1)
            exchange.close()
        }
        api.start()
        other.start()
        ApiConfig.baseUrlOverride = "http://127.0.0.1:${api.address.port}/base/"
        val client = HttpClientFactory.create(SyntheticTokens())
        try {
            client.get("http://127.0.0.1:${api.address.port}/base/resource")
            client.get("/api/products")
            client.get("http://127.0.0.1:${other.address.port}/resource")
            client.get("http://localhost:${other.address.port}/resource")
            client.get("http://127.0.0.1:${api.address.port}/outside")
            client.get("http://127.0.0.1:${api.address.port}/base/redirect")
            client.get("http://127.0.0.1:${api.address.port}/base/foreign-redirect")
            assertEquals(listOf(true), seen.filter { it.first == "api:/base/resource" }.map { it.second })
            assertEquals(listOf(true), seen.filter { it.first == "api:/base/api/products" }.map { it.second }, "relative API paths must retain backend prefix and bearer")
            assertEquals(3, seen.count { it.first == "other" }, "both direct requests and redirect must reach the test server once")
            assertEquals(false, seen.filter { it.first == "other" }.any { it.second }, "foreign origins must not receive Authorization, including after 401")
            assertEquals(listOf(false, false), seen.filter { it.first == "api:/outside" }.map { it.second }, "direct and redirected requests outside base path must not receive Authorization")
        } finally {
            client.close()
            api.stop(0)
            other.stop(0)
            ApiConfig.baseUrlOverride = null
        }
    }
}
