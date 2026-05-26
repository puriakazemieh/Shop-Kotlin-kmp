package com.kazemieh.network.order.dto.response

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class OrderDetailResponse(
    val id: Long,
    val status: String,
    val subtotalPrice: Double,
    val shippingPrice: Double,
    val totalPrice: Double,
    val items: List<OrderItemResponse>,
    val address: JsonElement? = null,
    val createdAt: String,
    val shippingCarrier: String? = null,
    val trackingCode: String? = null,
    val shippedAt: String? = null,
    val deliveredAt: String? = null
)
