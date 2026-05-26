package com.kazemieh.domain.order

import com.kazemieh.common.AppResult
import com.kazemieh.domain.order.OrderRepository

class CancelOrderUseCase(private val repository: OrderRepository) {
    suspend operator fun invoke(id: Long): AppResult<Unit> =
        repository.cancelOrder(id)
}
