package com.kazemieh.network.common

import io.ktor.client.request.get
import io.ktor.http.Url
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BrowserFetchSecurityTest {
    @Test
    fun `wordpress subdirectory paths survive browser request construction`(): Promise<Unit> = MainScope().promise {
        val global = js("globalThis")
        val original = global.fetch
        val seen = mutableListOf<String>()
        val hasAuthorization = mutableListOf<Boolean>()
        global.fetch = { input: dynamic, init: dynamic ->
            seen.add(input.toString())
            val headers = js("new Headers(init.headers)")
            hasAuthorization.add(headers.has("Authorization") as Boolean)
            js("Promise.resolve(new Response('', {status: 200}))")
        }
        ApiConfig.baseUrlOverride = "https://api.example.test/base/"
        val tokenProvider = object : TokenProvider {
            override fun saveTokens(accessToken: String, refreshToken: String) = Unit
            override fun getAccessToken(): String? = "synthetic-access"
            override fun getRefreshToken(): String? = null
            override fun clearTokens() = Unit
            override fun setOnTokenExpiredListener(listener: () -> Unit) = Unit
            override fun notifyTokenExpired() = Unit
        }
        val client = HttpClientFactory.create(tokenProvider)
        try {
            assertTrue(ApiConfig.isApprovedApiUrl(Url("https://api.example.test/base/protected")))
            assertFalse(ApiConfig.isApprovedApiUrl(Url("https://api.example.test/base/%2E%2E/secret")))
            assertFalse(ApiConfig.isApprovedApiUrl(Url("https://api.example.test:444/base/protected")))
            client.get("https://api.example.test/base/protected")
            client.get("/api/products")
            client.get("https://other.example.test/base/protected")
            assertEquals(listOf("https://api.example.test/base/protected", "https://api.example.test/base/api/products", "https://other.example.test/base/protected"), seen)
            assertEquals(listOf(true, true, false), hasAuthorization)
        } finally {
            client.close(); ApiConfig.baseUrlOverride = null; global.fetch = original
        }
    }

    @Test
    fun `fetch blocks automatic redirects and retains credentials setting`(): Promise<Unit> = MainScope().promise {
        val global = js("globalThis")
        val original = global.fetch
        var observedRedirect: String? = null
        var observedCredentials: String? = null
        global.fetch = { _: dynamic, init: dynamic ->
            observedRedirect = init.redirect as String?
            observedCredentials = init.credentials as String?
            js("Promise.resolve(new Response('', {status: 200}))")
        }
        ApiConfig.baseUrlOverride = "https://api.example.test/base/"
        val tokenProvider = object : TokenProvider {
            override fun saveTokens(accessToken: String, refreshToken: String) = Unit
            override fun getAccessToken(): String? = null
            override fun getRefreshToken(): String? = null
            override fun clearTokens() = Unit
            override fun setOnTokenExpiredListener(listener: () -> Unit) = Unit
            override fun notifyTokenExpired() = Unit
        }
        val client = HttpClientFactory.create(tokenProvider)
        try {
            client.get("https://api.example.test/base/resource")
            assertEquals("error", observedRedirect)
            assertEquals("include", observedCredentials)
        } finally {
            client.close()
            ApiConfig.baseUrlOverride = null
            global.fetch = original
        }
    }
}
