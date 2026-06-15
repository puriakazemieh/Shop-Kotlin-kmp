package com.kazemieh.network.wallet.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class WithdrawRequest(
    val amount: Double,
    val iban: String
)
