package com.kazemieh.network.dto.response

import kotlinx.serialization.Serializable


@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse,
)