package com.kazemieh.network.dto.order.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateOrderRequest(
    val items: List<OrderItemRequest>,
    val addressId: Long? = null
)
