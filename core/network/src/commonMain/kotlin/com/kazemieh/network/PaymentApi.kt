package com.kazemieh.network

import com.kazemieh.network.dto.payment.request.PaymentRequest
import com.kazemieh.network.dto.payment.response.PaymentResponse

interface PaymentApi {
    suspend fun requestPayment(request: PaymentRequest): PaymentResponse
}
