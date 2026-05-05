package com.kazemieh.data.auth.datasource

import com.kazemieh.common.AppResult
import com.kazemieh.common.safeApiCall
import com.kazemieh.data.auth.mapper.toDomain
import com.kazemieh.domain.model.Auth
import com.kazemieh.network.AuthApi
import com.kazemieh.network.dto.request.LoginRequest
import com.kazemieh.network.dto.request.RegisterRequest

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

}