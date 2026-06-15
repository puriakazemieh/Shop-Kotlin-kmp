package com.kazemieh.network.auth.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class LoginWithOtpRequest(
    val mobile: String,
    val otpCode: String
)
