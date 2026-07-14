package com.kazemieh.network.order.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class OrderResponse(
    val id: Long,
    val status: String,
    val subtotalPrice: Double,
    val shippingPrice: Double,
    val totalPrice: Double,
    val itemCount: Int = 0,
    val createdAt: String
)
