package com.kazemieh.network.auth.dto.request

import com.kazemieh.network.auth.dto.*

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)
