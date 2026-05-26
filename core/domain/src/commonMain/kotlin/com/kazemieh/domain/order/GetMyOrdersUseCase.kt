package com.kazemieh.domain.order

import com.kazemieh.common.AppResult
import com.kazemieh.domain.order.Order
import com.kazemieh.domain.order.OrderRepository
import kotlinx.coroutines.flow.Flow

class GetMyOrdersUseCase(private val repository: OrderRepository) {
    operator fun invoke(): Flow<AppResult<List<Order>>> = repository.getMyOrders()
}
