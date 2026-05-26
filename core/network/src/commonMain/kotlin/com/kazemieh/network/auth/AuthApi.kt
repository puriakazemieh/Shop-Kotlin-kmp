package com.kazemieh.network.auth

import com.kazemieh.network.common.PageResponse

import com.kazemieh.network.auth.dto.request.LoginRequest
import com.kazemieh.network.auth.dto.request.RegisterRequest
import com.kazemieh.network.auth.dto.request.ResetPasswordRequest
import com.kazemieh.network.auth.dto.response.AuthResponse
import com.kazemieh.network.profile.dto.response.UserResponse

interface AuthApi {

    suspend fun login(request: LoginRequest): AuthResponse

    suspend fun register(request: RegisterRequest): AuthResponse

    suspend fun forgotPassword(email: String)

    suspend fun resetPassword(request: ResetPasswordRequest)


}
