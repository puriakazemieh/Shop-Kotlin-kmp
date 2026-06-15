package com.kazemieh.network.auth.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordWithOtpRequest(
    val mobile: String,
    val otpCode: String,
    val newPassword: String
)
