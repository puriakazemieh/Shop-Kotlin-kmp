package com.kazemieh.network.admin.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class AdminWithdrawalResponse(
    val id: Long,
    val userId: Long,
    val userFullName: String? = null,
    val userEmail: String? = null,
    val amount: Double,
    val iban: String,
    val status: String,
    val adminNote: String? = null,
    val createdAt: String
)
