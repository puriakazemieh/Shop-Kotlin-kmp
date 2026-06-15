package com.kazemieh.network.wallet.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class WalletBalanceResponse(
    val balance: Double,
    val userId: Long
)
