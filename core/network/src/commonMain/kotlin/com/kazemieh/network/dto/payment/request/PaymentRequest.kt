package com.kazemieh.network.dto.payment.request

import kotlinx.serialization.Serializable

@Serializable
data class PaymentRequest(
    val orderId: String
)
