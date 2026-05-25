package com.kazemieh.data.payment.repository

import com.kazemieh.common.AppResult
import com.kazemieh.common.map
import com.kazemieh.data.payment.datasource.PaymentDataSource
import com.kazemieh.domain.repository.PaymentRepository

class PaymentRepositoryImpl(
    private val dataSource: PaymentDataSource
) : PaymentRepository {
    override suspend fun requestPayment(orderId: Long): AppResult<String> =
        dataSource.requestPayment(orderId).map { it.paymentUrl }
}
