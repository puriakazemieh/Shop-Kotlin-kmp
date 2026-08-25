package com.kazemieh.network.auth.dto.response

import com.kazemieh.network.profile.dto.response.UserResponse

import com.kazemieh.network.auth.dto.*

import kotlinx.serialization.Serializable


@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String = "",
    val user: UserResponse,
)
