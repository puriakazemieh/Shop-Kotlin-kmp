package com.kazemieh.domain.usecase.order

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.Order
import com.kazemieh.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow

class GetMyOrdersUseCase(private val repository: OrderRepository) {
    operator fun invoke(): Flow<AppResult<List<Order>>> = repository.getMyOrders()
}
