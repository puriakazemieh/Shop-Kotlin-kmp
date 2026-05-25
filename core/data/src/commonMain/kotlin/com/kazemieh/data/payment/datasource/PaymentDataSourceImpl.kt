package com.kazemieh.data.payment.datasource

import com.kazemieh.common.AppResult
import com.kazemieh.network.PaymentApi
import com.kazemieh.network.dto.payment.request.PaymentRequest
import com.kazemieh.network.dto.payment.response.PaymentResponse
import com.kazemieh.network.safeApiCall

class PaymentDataSourceImpl(
    private val api: PaymentApi
) : PaymentDataSource {
    override suspend fun requestPayment(orderId: Long): AppResult<PaymentResponse> = safeApiCall {
        api.requestPayment(PaymentRequest(orderId.toString()))
    }
}
