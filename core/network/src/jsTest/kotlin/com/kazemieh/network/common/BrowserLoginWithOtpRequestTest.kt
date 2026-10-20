package com.kazemieh.network.common

import com.kazemieh.network.auth.AuthApiImpl
import com.kazemieh.network.auth.dto.request.LoginRequest
import com.kazemieh.network.auth.dto.request.RegisterRequest
import com.kazemieh.network.auth.dto.request.LoginWithOtpRequest
import io.ktor.client.HttpClient
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.promise
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.test.assertEquals

class BrowserLoginWithOtpRequestTest {
    @Test
    fun `browser password and OTP authentication use WordPress guarded routes`(): Promise<Unit> = MainScope().promise {
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
            val api = AuthApiImpl(client)
            api.login(LoginRequest("synthetic@example.test", "safe-password"))
            api.register(RegisterRequest(email = "synthetic@example.test", password = "safe-password"))
            api.loginWithOtp(LoginWithOtpRequest("09000000000", "123456"))
            assertEquals(
                listOf(
                    "https://api.example.test/base/api/auth/login",
                    "https://api.example.test/base/api/auth/register",
                    "https://api.example.test/base/api/auth/login-with-otp"
                ),
                urls
            )
        } finally {
            client.close()
            ApiConfig.baseUrlOverride = null
            global.fetch = original
        }
    }
}
