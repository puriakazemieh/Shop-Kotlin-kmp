package com.kazemieh.network.common

import com.kazemieh.network.auth.AuthApiImpl
import com.kazemieh.network.auth.dto.request.LoginWithOtpRequest
import io.ktor.client.HttpClient
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.test.assertEquals

class BrowserLoginWithOtpRequestTest {
    @Test
    fun `OTP login uses the WordPress guarded route`(): Promise<Unit> = MainScope().promise {
        val global = js("globalThis")
        val original = global.fetch
        val urls = mutableListOf<String>()
        global.fetch = { input: dynamic, _: dynamic ->
            urls.add(input.toString())
            js("Promise").resolve(
                js("new Response('{\"accessToken\":\"synthetic\",\"refreshToken\":\"\",\"user\":{\"id\":1,\"email\":null,\"mobile\":null,\"city\":null,\"postalCode\":null,\"role\":\"customer\"}}', {status: 200})")
            )
        }
        ApiConfig.baseUrlOverride = "https://api.example.test/base/"
        val client = HttpClientFactory.create(object : TokenProvider {
            override fun saveTokens(accessToken: String, refreshToken: String) = Unit
            override fun getAccessToken(): String? = null
            override fun getRefreshToken(): String? = null
            override fun clearTokens() = Unit
            override fun setOnTokenExpiredListener(listener: () -> Unit) = Unit
            override fun notifyTokenExpired() = Unit
        })
        try {
            AuthApiImpl(client).loginWithOtp(LoginWithOtpRequest("09000000000", "123456"))
            assertEquals(
                listOf("https://api.example.test/base/api/auth/login-with-otp"),
                urls
            )
        } finally {
            client.close()
            ApiConfig.baseUrlOverride = null
            global.fetch = original
        }
    }
}
