package com.kazemieh.network.auth.dto.request

import com.kazemieh.network.auth.dto.*

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)
