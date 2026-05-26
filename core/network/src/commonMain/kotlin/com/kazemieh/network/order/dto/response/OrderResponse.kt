package com.kazemieh.network.order.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class OrderResponse(
    val id: Long,
    val status: String,
    val totalPrice: Double,
    val createdAt: String,
    val itemCount: Int
)
