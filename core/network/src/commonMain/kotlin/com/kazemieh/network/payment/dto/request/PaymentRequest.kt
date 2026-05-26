package com.kazemieh.network.payment.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class PaymentRequest(
    val orderId: String
)
