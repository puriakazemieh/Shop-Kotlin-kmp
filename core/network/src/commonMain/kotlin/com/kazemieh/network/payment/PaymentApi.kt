package com.kazemieh.network.payment

import com.kazemieh.network.payment.dto.request.*
import com.kazemieh.network.payment.dto.response.*

import com.kazemieh.network.common.PageResponse

import com.kazemieh.network.payment.dto.request.PaymentRequest
import com.kazemieh.network.payment.dto.response.PaymentResponse

interface PaymentApi {
    suspend fun requestPayment(request: PaymentRequest): PaymentResponse
}
