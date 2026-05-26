package com.kazemieh.network.payment.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class PaymentResponse(
    val paymentUrl: String
)
