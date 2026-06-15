package com.kazemieh.network.admin.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class AdminWithdrawalResponse(
    val id: Long,
    val userId: Long,
    val amount: Double,
    val iban: String,
    val status: String,
    val createdAt: String
)
