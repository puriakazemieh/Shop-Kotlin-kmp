package com.kazemieh.network.admin.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminProcessWithdrawalRequest(
    val status: String,
    val adminNote: String?
)
