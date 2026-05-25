package com.kazemieh.network

import com.kazemieh.network.dto.payment.request.PaymentRequest
import com.kazemieh.network.dto.payment.response.PaymentResponse
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
