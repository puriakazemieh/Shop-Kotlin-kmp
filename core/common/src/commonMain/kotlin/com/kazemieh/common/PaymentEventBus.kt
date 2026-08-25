package com.kazemieh.common

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object PaymentEventBus {
    private val _events = MutableSharedFlow<PaymentResult>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<PaymentResult> = _events.asSharedFlow()

    fun publish(result: PaymentResult) {
        _events.tryEmit(result)
    }

    fun reset() {
        _events.tryEmit(PaymentResult(null))
    }
}

data class PaymentResult(
    val token: String?
)
