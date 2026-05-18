package com.kazemieh.domain.usecase.order

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.OrderDetail
import com.kazemieh.domain.repository.OrderRepository

class CreateOrderUseCase(private val repository: OrderRepository) {
    suspend operator fun invoke(items: List<Pair<Long, Int>>, addressId: Long? = null): AppResult<OrderDetail> =
        repository.createOrder(items, addressId)
}
