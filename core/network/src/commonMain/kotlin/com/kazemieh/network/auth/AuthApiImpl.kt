package com.kazemieh.network.auth

import com.kazemieh.network.common.PageResponse

import com.kazemieh.network.common.safeApiCallRaw

import com.kazemieh.network.auth.dto.request.LoginRequest
import com.kazemieh.network.auth.dto.request.RegisterRequest
import com.kazemieh.network.auth.dto.request.ResetPasswordRequest
import com.kazemieh.network.auth.dto.request.SendLoginOtpRequest
import com.kazemieh.network.auth.dto.request.LoginWithOtpRequest
import com.kazemieh.network.auth.dto.request.ForgotPasswordRequest
import com.kazemieh.network.auth.dto.request.ResetPasswordWithOtpRequest
import com.kazemieh.network.auth.dto.response.AuthResponse
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
        request: ForgotPasswordRequest
    ) = safeApiCallRaw<Unit> {
        client.post("api/auth/forgot-password") { setBody(request) }
    }

    override suspend fun resetPassword(
        request: ResetPasswordRequest
    ) = safeApiCallRaw<Unit> {
        client.post("api/auth/reset-password") { setBody(request) }
    }

    override suspend fun sendLoginOtp(request: SendLoginOtpRequest) = safeApiCallRaw<Unit> {
        client.post("api/auth/send-login-otp") { setBody(request) }
    }

    override suspend fun loginWithOtp(request: LoginWithOtpRequest): AuthResponse = safeApiCallRaw {
        client.post("api/auth/login-with-otp") { setBody(request) }
    }

    override suspend fun resetPasswordWithOtp(request: ResetPasswordWithOtpRequest) = safeApiCallRaw<Unit> {
        client.post("api/auth/reset-password-with-otp") { setBody(request) }
    }
}
