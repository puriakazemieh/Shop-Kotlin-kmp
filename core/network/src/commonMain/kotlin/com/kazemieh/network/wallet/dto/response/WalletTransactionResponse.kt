package com.kazemieh.network.wallet.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class WalletTransactionResponse(
    val id: Long,
    val amount: Double,
    val type: String,
    val description: String?,
    val referenceId: String?,
    val createdAt: String
)
