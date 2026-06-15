package com.kazemieh.data.auth.source

import com.kazemieh.network.auth.AuthApi
import com.kazemieh.data.auth.mapper.toDomain
import com.kazemieh.network.auth.dto.request.*
import com.kazemieh.network.auth.dto.response.*
import com.kazemieh.domain.auth.*
import com.kazemieh.network.common.*
import com.kazemieh.common.*



class AuthDataSourceImpl(
    private val authApi: AuthApi
) : AuthDataSource {

    override suspend fun login(email: String, password: String): AppResult<Auth> = safeApiCall {
        authApi.login(LoginRequest(username = email, password = password)).toDomain()
    }

    override suspend fun register(email: String?, mobile: String?, password: String): AppResult<Auth> = safeApiCall {
        authApi.register(RegisterRequest(email = email, mobile = mobile, password = password)).toDomain()
    }

    override suspend fun forgotPassword(email: String?, mobile: String?): AppResult<Unit> = safeApiCall {
        authApi.forgotPassword(ForgotPasswordRequest(email = email, mobile = mobile))
    }

    override suspend fun resetPassword(token: String, newPassword: String): AppResult<Unit> = safeApiCall {
        authApi.resetPassword(ResetPasswordRequest(token, newPassword))
    }

    override suspend fun sendLoginOtp(mobile: String): AppResult<Unit> = safeApiCall {
        authApi.sendLoginOtp(SendLoginOtpRequest(mobile))
    }

    override suspend fun loginWithOtp(mobile: String, otpCode: String): AppResult<Auth> = safeApiCall {
        authApi.loginWithOtp(LoginWithOtpRequest(mobile, otpCode)).toDomain()
    }

    override suspend fun resetPasswordWithOtp(mobile: String, otpCode: String, newPassword: String): AppResult<Unit> = safeApiCall {
        authApi.resetPasswordWithOtp(ResetPasswordWithOtpRequest(mobile, otpCode, newPassword))
    }

}
