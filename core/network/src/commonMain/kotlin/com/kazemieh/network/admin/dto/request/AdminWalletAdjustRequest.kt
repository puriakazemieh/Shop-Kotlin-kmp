package com.kazemieh.network.admin.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminWalletAdjustRequest(
    val userId: Long,
    val amount: Double,
    val description: String?
)
