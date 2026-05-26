package com.kazemieh.data.payment.repository

import com.kazemieh.network.payment.dto.request.*
import com.kazemieh.network.payment.dto.response.*
import com.kazemieh.domain.payment.*
import com.kazemieh.network.common.*
import com.kazemieh.common.*
import com.kazemieh.data.payment.source.PaymentDataSource



class PaymentRepositoryImpl(
    private val dataSource: PaymentDataSource
) : PaymentRepository {
    override suspend fun requestPayment(orderId: Long): AppResult<String> =
        dataSource.requestPayment(orderId).map { it.paymentUrl }
}
