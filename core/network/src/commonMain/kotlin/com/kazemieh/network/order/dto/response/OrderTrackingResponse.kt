package com.kazemieh.network.order.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class OrderTrackingResponse(
    val id: Long,
    val status: String,
    val trackingCode: String?,
    val orderedAt: String,
    val shippedAt: String?,
    val history: List<OrderStatusHistoryItemDto> = emptyList()
)

@Serializable
data class OrderStatusHistoryItemDto(
    val status: String,
    val at: String
)
