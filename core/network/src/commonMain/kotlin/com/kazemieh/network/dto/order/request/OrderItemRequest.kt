package com.kazemieh.network.dto.order.request

import kotlinx.serialization.Serializable

@Serializable
data class OrderItemRequest(
    val variantId: Long,
    val qty: Int
)
