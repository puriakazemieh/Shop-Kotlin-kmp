package com.kazemieh.network.admin.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class AdminWalletUserResponse(
    val userId: Long,
    val email: String,
    val fullName: String,
    val balance: Double
)
