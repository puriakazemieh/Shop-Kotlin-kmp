package com.kazemieh.network.wallet.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class TopUpRequest(
    val amount: Double
)
