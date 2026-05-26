package com.kazemieh.network.payment

import com.kazemieh.network.payment.dto.request.*
import com.kazemieh.network.payment.dto.response.*

import com.kazemieh.network.common.PageResponse

import com.kazemieh.network.common.safeApiCallRaw

import com.kazemieh.network.payment.dto.request.PaymentRequest
import com.kazemieh.network.payment.dto.response.PaymentResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.*

class PaymentApiImpl(
    private val client: HttpClient
) : PaymentApi {
    override suspend fun requestPayment(request: PaymentRequest): PaymentResponse = safeApiCallRaw {
        client.post("api/payment/request") {
            setBody(request)
        }
    }
}
