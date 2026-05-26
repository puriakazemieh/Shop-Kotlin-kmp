package com.kazemieh.data.payment.source

import com.kazemieh.network.payment.dto.request.*
import com.kazemieh.network.payment.dto.response.*
import com.kazemieh.domain.payment.*
import com.kazemieh.network.common.*
import com.kazemieh.common.*



interface PaymentDataSource {
    suspend fun requestPayment(orderId: Long): AppResult<PaymentResponse>
}
