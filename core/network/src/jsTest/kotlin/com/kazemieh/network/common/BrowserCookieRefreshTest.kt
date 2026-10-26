package com.kazemieh.network.common

import com.kazemieh.network.auth.AuthApiImpl
import io.ktor.client.request.get
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.test.assertEquals

class BrowserCookieRefreshTest {
    private fun resolvedResponse(url: String, response: dynamic): dynamic {
        // Real Fetch responses carry their URL; a constructed Response defaults to an empty URL.
        val descriptor = js("({})")
        descriptor.value = url
        js("Object").defineProperty(response, "url", descriptor)
        return js("Promise").resolve(response)
    }
    private class SyntheticTokens : TokenProvider {
        var access: String? = "synthetic-old-access"
        var storedRefresh: String? = null
        var expired = 0
        override fun saveTokens(accessToken: String, refreshToken: String) { access = accessToken; storedRefresh = refreshToken }
        override fun getAccessToken() = access
        override fun getRefreshToken() = storedRefresh
        override fun clearTokens() { access = null; storedRefresh = null }
        override fun setOnTokenExpiredListener(listener: () -> Unit) = Unit
        override fun notifyTokenExpired() { expired++ }
    }

    @Test
    fun `startup restoration uses the guarded cookie refresh endpoint`(): Promise<Unit> = MainScope().promise {
        val global = js("globalThis")
        val original = global.fetch
        var credentials: String? = null
        var requestHeader: String? = null
        var requestBody: String? = null
        global.fetch = { input: dynamic, init: dynamic ->
            val url = input.toString() as String
            assertEquals("https://api.example.test/base/api/auth/web/refresh", url)
            credentials = init.credentials as String?
            val headers = js("new Headers(init.headers)")
            requestHeader = headers.get("X-Carmilla-Web-Request") as String?
            requestBody = init.body as String?
            resolvedResponse(url, js("new Response('{\"accessToken\":\"startup-access\"}', {status: 200, headers: {'Content-Type':'application/json'}})"))
        }
        ApiConfig.baseUrlOverride = "https://api.example.test/base/"
        val client = HttpClientFactory.create(SyntheticTokens())
        try {
            val response = AuthApiImpl(client).refreshBrowserSession()
            assertEquals("startup-access", response.accessToken)
            assertEquals("", response.refreshToken)
            assertEquals("include", credentials)
            assertEquals("1", requestHeader)
            assertEquals(null, requestBody)
        } finally {
            client.close(); ApiConfig.baseUrlOverride = null; global.fetch = original
        }
    }

    @Test
    fun `cookie refresh succeeds without a javascript refresh token`(): Promise<Unit> = MainScope().promise {
        val global = js("globalThis")
        val original = global.fetch
        val urls = mutableListOf<String>()
        var refreshCredentials: String? = null
        var refreshBody: String? = null
        var refreshHeader: String? = null
        global.fetch = { input: dynamic, init: dynamic ->
            val url = input.toString() as String
            urls.add(url)
            when {
                url.contains("/api/auth/web/refresh") -> {
                    refreshCredentials = init.credentials as String?
                    refreshBody = init.body as String?
                    val headers = js("new Headers(init.headers)")
                    refreshHeader = headers.get("X-Carmilla-Web-Request") as String?
                    resolvedResponse(url, js("new Response('{\"accessToken\":\"synthetic-new-access\"}', {status: 200, headers: {'Content-Type':'application/json'}})"))
                }
                urls.count { it.contains("/protected") } == 1 ->
                    resolvedResponse(url, js("new Response('', {status: 401, headers: {'WWW-Authenticate':'Bearer'}})"))
                else -> resolvedResponse(url, js("new Response('', {status: 200})"))
            }
        }
        ApiConfig.baseUrlOverride = "https://api.example.test/base/"
        val tokens = SyntheticTokens()
        val client = HttpClientFactory.create(tokens)
        try {
            val status = client.get("https://api.example.test/base/protected").status.value
            assertEquals("https://api.example.test/base/protected", urls.first(), "initial browser request")
            assertEquals(200, status, "synthetic request URLs: ${urls.joinToString()}")
            assertEquals(1, urls.count { it.contains("/api/auth/web/refresh") })
            assertEquals("include", refreshCredentials)
            assertEquals("1", refreshHeader)
            assertEquals(null, refreshBody)
            assertEquals("synthetic-new-access", tokens.access)
            assertEquals("", tokens.storedRefresh)
            assertEquals(0, tokens.expired)
        } finally {
            client.close(); ApiConfig.baseUrlOverride = null; global.fetch = original
        }
    }

    @Test
    fun `error or exposed browser refresh credential cannot authenticate`(): Promise<Unit> = MainScope().promise {
        val global = js("globalThis")
        val original = global.fetch
        ApiConfig.baseUrlOverride = "https://api.example.test/base/"
        try {
            for (testCase in 0..2) {
                global.fetch = { input: dynamic, _: dynamic ->
                    val url = input.toString() as String
                    if (url.contains("/api/auth/web/refresh")) {
                        when (testCase) {
                            0 -> resolvedResponse(url, js("new Response('{\"accessToken\":\"synthetic-new-access\"}', {status: 503, headers: {'Content-Type':'application/json'}})"))
                            1 -> resolvedResponse(url, js("new Response('{\"accessToken\":\"\"}', {status: 200, headers: {'Content-Type':'application/json'}})"))
                            else -> resolvedResponse(url, js("new Response('{\"accessToken\":\"synthetic-new-access\",\"refreshToken\":\"synthetic-exposed\"}', {status: 200, headers: {'Content-Type':'application/json'}})"))
                        }
                    } else resolvedResponse(url, js("new Response('', {status: 401, headers: {'WWW-Authenticate':'Bearer'}})"))
                }
                val tokens = SyntheticTokens()
                val client = HttpClientFactory.create(tokens)
                try {
                    assertEquals(401, client.get("https://api.example.test/base/protected").status.value)
                    assertEquals(null, tokens.access)
                    assertEquals(null, tokens.storedRefresh)
                    assertEquals(1, tokens.expired)
                } finally { client.close() }
            }
        } finally { ApiConfig.baseUrlOverride = null; global.fetch = original }
    }
}
