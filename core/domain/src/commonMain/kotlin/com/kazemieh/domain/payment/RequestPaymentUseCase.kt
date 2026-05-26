package com.kazemieh.domain.payment

import com.kazemieh.common.AppResult
import com.kazemieh.domain.payment.PaymentRepository

class RequestPaymentUseCase(
    private val repository: PaymentRepository
) {
    suspend operator fun invoke(orderId: Long): AppResult<String> {
        return repository.requestPayment(orderId)
    }
}
