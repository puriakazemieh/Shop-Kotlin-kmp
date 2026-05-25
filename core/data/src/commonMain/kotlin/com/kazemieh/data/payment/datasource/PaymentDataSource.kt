package com.kazemieh.data.payment.datasource

import com.kazemieh.common.AppResult
import com.kazemieh.network.dto.payment.response.PaymentResponse

interface PaymentDataSource {
    suspend fun requestPayment(orderId: Long): AppResult<PaymentResponse>
}
