package com.kazemieh.network.dto.order.response

import kotlinx.serialization.Serializable

@Serializable
data class OrderItemResponse(
    val id: Long? = null,
    val variantId: Long,
    val qty: Int,
    val unitPrice: Double,
    val title: String,
    val options: Map<String, String>
)
