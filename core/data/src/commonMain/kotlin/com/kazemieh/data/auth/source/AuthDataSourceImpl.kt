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
        authApi.login(LoginRequest(email, password)).toDomain()
    }

    override suspend fun register(email: String, password: String): AppResult<Auth> = safeApiCall {
        authApi.register(RegisterRequest(email, password)).toDomain()
    }

    override suspend fun forgotPassword(email: String): AppResult<Unit> = safeApiCall {
        authApi.forgotPassword(email)
    }

    override suspend fun resetPassword(token: String, newPassword: String): AppResult<Unit> = safeApiCall {
        authApi.resetPassword(ResetPasswordRequest(token, newPassword))
    }

}
