package com.kazemieh.domain.repository

import com.kazemieh.common.AppResult

interface PaymentRepository {
    suspend fun requestPayment(orderId: Long): AppResult<String>
}
