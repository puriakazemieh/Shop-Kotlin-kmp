package com.kazemieh.data

import com.kazemieh.data.mapper.toDomain
import com.kazemieh.domain.common.AppResult
import com.kazemieh.domain.common.safeApiCall
import com.kazemieh.domain.model.User
import com.kazemieh.network.ApiClient
import com.kazemieh.network.ApiRoutes
import com.kazemieh.network.dto.request.LoginRequest
import com.kazemieh.network.dto.request.RegisterRequest
import com.kazemieh.network.dto.response.UserResponse

class RemoteDataSourceImpl(
    private val client: ApiClient
) : RemoteDataSource {

    override suspend fun login(
        email: String,
        password: String
    ): AppResult<User> {
        return safeApiCall {
            val dto: UserResponse = client.post(
                ApiRoutes.LOGIN,
                LoginRequest(email, password)
            )

            dto.toDomain()
        }
    }

    override suspend fun register(
        email: String,
        password: String
    ): AppResult<User> {
        return safeApiCall {
            val dto: UserResponse = client.post(
                ApiRoutes.REGISTER,
                RegisterRequest(email, password)
            )

            dto.toDomain()
        }
    }

    override suspend fun forgotPassword(
        email: String
    ): AppResult<Unit> {
        return safeApiCall {
            client.post<Map<String, String>, Unit>(
                ApiRoutes.FORGOT_PASSWORD,
                mapOf("email" to email)
            )
        }
    }
}