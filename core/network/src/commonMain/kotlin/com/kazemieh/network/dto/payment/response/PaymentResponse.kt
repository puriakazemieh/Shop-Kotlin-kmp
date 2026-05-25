package com.kazemieh.network.dto.payment.response

import kotlinx.serialization.Serializable

@Serializable
data class PaymentResponse(
    val paymentUrl: String
)
