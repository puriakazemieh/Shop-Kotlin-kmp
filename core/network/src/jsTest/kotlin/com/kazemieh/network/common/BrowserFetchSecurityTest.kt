package com.kazemieh.network.common

import io.ktor.client.request.get
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.test.assertEquals

class BrowserFetchSecurityTest {
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
