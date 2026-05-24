package com.kazemieh.network

import com.kazemieh.network.dto.request.LoginRequest
import com.kazemieh.network.dto.request.RegisterRequest
import com.kazemieh.network.dto.request.ResetPasswordRequest
import com.kazemieh.network.dto.response.AuthResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AuthApiImpl(
    private val client: HttpClient
) : AuthApi {

    //puriakazemieh@gmail.com
    override suspend fun login(
        request: LoginRequest
    ): AuthResponse = safeApiCallRaw {
        client.post("api/auth/login") { setBody(request) }
    }

    override suspend fun register(
        request: RegisterRequest
    ): AuthResponse = safeApiCallRaw {
        client.post("api/auth/register") { setBody(request) }
    }

    override suspend fun forgotPassword(
        email: String
    ) = safeApiCallRaw<Unit> {
        client.post("api/auth/forgot-password") { setBody(mapOf("email" to email)) }
    }

    override suspend fun resetPassword(
        request: ResetPasswordRequest
    ) = safeApiCallRaw<Unit> {
        client.post("api/auth/reset-password") { setBody(request) }
    }
}