package com.kazemieh.domain.order

import com.kazemieh.common.AppResult
import com.kazemieh.domain.order.OrderDetail
import com.kazemieh.domain.order.OrderRepository

class GetOrderUseCase(private val repository: OrderRepository) {
    suspend operator fun invoke(id: Long): AppResult<OrderDetail> =
        repository.getOrder(id)
}
