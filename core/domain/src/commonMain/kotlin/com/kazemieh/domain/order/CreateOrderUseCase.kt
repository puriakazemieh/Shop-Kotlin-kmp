package com.kazemieh.domain.order

import com.kazemieh.common.AppResult
import com.kazemieh.domain.order.OrderDetail
import com.kazemieh.domain.order.OrderRepository

class CreateOrderUseCase(private val repository: OrderRepository) {
    suspend operator fun invoke(
        items: List<Pair<Long, Int>>,
        addressId: Long? = null,
        useWallet: Boolean = false,
        isGift: Boolean = false,
        giftMessage: String? = null
    ): AppResult<OrderDetail> =
        repository.createOrder(items, addressId, useWallet, isGift, giftMessage)
}
