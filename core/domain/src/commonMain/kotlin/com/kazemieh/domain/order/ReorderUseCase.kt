package com.kazemieh.domain.order

import com.kazemieh.common.AppResult

class ReorderUseCase(private val repository: OrderRepository) {
    suspend operator fun invoke(orderId: Long): AppResult<ReorderResult> = repository.reorder(orderId)
}
