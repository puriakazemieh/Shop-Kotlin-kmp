package com.kazemieh.network.auth.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class SendLoginOtpRequest(
    val mobile: String
)
