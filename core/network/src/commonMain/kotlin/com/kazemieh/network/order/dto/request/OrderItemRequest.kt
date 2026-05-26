package com.kazemieh.network.order.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class OrderItemRequest(
    val variantId: Long,
    val qty: Int
)
