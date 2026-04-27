package com.kazemieh.data

import com.kazemieh.data.mapper.toDomain
import com.kazemieh.domain.common.AppResult
import com.kazemieh.domain.common.safeApiCall
import com.kazemieh.domain.model.User
import com.kazemieh.network.AuthApi
import com.kazemieh.network.dto.request.LoginRequest
import com.kazemieh.network.dto.request.RegisterRequest

class RemoteDataSourceImpl(
    private val authApi: AuthApi
) : RemoteDataSource {

    override suspend fun login(email: String, password: String): AppResult<User> = safeApiCall {
        authApi.login(LoginRequest(email, password)).toDomain()
    }

    override suspend fun register(email: String, password: String): AppResult<User> = safeApiCall {
        authApi.register(RegisterRequest(email, password)).toDomain()
    }

    override suspend fun forgotPassword(email: String): AppResult<Unit> = safeApiCall {
        authApi.forgotPassword(email)
    }

}