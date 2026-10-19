package com.kazemieh.network.common

import com.kazemieh.network.auth.AuthApiImpl
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BrowserLogoutRequestTest {
    @Test
    fun `logout uses cookie without bearer and never refreshes on failure`(): Promise<Unit> = MainScope().promise {
        val global = js("globalThis")
        val original = global.fetch
        val urls = mutableListOf<String>()
        val auth = mutableListOf<String?>()
        val guards = mutableListOf<String?>()
        val credentials = mutableListOf<String?>()
        val bodies = mutableListOf<String?>()
        var status = 200
        global.fetch = { input: dynamic, init: dynamic ->
            urls.add(input.toString())
            val headers = js("new Headers(init.headers)")
            auth.add(headers.get("Authorization") as String?)
            guards.add(headers.get("X-Carmilla-Web-Request") as String?)
            credentials.add(init.credentials as String?)
            bodies.add(init.body as String?)
            val responseOptions = js("({})")
            responseOptions.status = status
            val response = js("new Response('', responseOptions)")
            val descriptor = js("({})"); descriptor.value = input.toString()
            js("Object").defineProperty(response, "url", descriptor)
            js("Promise").resolve(response)
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
        val api = AuthApiImpl(client)
        try {
            api.logoutWeb()
            status = 401
            assertFailsWith<IllegalStateException> { api.logoutWeb() }
            status = 503
            assertFailsWith<IllegalStateException> { api.logoutWeb() }
            assertEquals(List(3) { "https://api.example.test/base/api/auth/web/logout" }, urls)
            assertEquals(List<String?>(3) { null }, auth)
            assertEquals(List<String?>(3) { "1" }, guards)
            assertEquals(List<String?>(3) { "include" }, credentials)
            assertEquals(List<String?>(3) { null }, bodies)
        } finally { client.close(); ApiConfig.baseUrlOverride = null; global.fetch = original }
    }
}
