package com.kazemieh.network.auth.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordRequest(
    val email: String? = null,
    val mobile: String? = null
)
