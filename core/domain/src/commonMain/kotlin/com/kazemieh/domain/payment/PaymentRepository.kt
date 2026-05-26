package com.kazemieh.domain.payment

import com.kazemieh.common.AppResult

interface PaymentRepository {
    suspend fun requestPayment(orderId: Long): AppResult<String>
}
