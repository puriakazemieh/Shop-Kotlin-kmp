package com.kazemieh.domain.usecase.order

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.OrderDetail
import com.kazemieh.domain.repository.OrderRepository

class GetOrderUseCase(private val repository: OrderRepository) {
    suspend operator fun invoke(id: Long): AppResult<OrderDetail> =
        repository.getOrder(id)
}
