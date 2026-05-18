package com.kazemieh.domain.usecase.order

import com.kazemieh.common.AppResult
import com.kazemieh.domain.repository.OrderRepository

class CancelOrderUseCase(private val repository: OrderRepository) {
    suspend operator fun invoke(id: Long): AppResult<Unit> =
        repository.cancelOrder(id)
}
