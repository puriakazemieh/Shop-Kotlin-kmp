package com.kazemieh.cart.payment_completed

import com.kazemieh.common.AppResult

data class PaymentState(
    val result: AppResult<Unit> = AppResult.Loading
)

sealed interface PaymentIntent {
    data class VerifyPayment(val orderId: Long?) : PaymentIntent
    data object OnBackClick : PaymentIntent
}

sealed interface PaymentEffect {
    data object NavigateBack : PaymentEffect
}
