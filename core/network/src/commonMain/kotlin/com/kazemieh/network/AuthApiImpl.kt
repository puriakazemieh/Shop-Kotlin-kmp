package com.kazemieh.network

import com.kazemieh.network.dto.request.LoginRequest
import com.kazemieh.network.dto.request.RegisterRequest
import com.kazemieh.network.dto.response.AuthResponse
import com.kazemieh.network.dto.response.UserResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AuthApiImpl(
    private val client: HttpClient
) : AuthApi {

    //puriakazemieh@gmail.com
    override suspend fun login(
        request: LoginRequest
    ): AuthResponse =
        client.post("api/auth/login") { setBody(request) }.body()

    override suspend fun register(
        request: RegisterRequest
    ): AuthResponse =
        client.post("api/auth/register") { setBody(request) }.body()

    override suspend fun forgotPassword(
        email: String
    ) {
        client.post("api/auth/forgot-password") { setBody(mapOf("email" to email)) }
    }
}