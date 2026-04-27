package com.kazemieh.network

import com.kazemieh.network.dto.request.LoginRequest
import com.kazemieh.network.dto.request.RegisterRequest
import com.kazemieh.network.dto.response.UserResponse

interface AuthApi {

    suspend fun login(request: LoginRequest): UserResponse

    suspend fun register(request: RegisterRequest): UserResponse

    suspend fun forgotPassword(email: String)


}