package com.kazemieh.network.order.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class OrderDetailResponse(
    val id: Long,
    val status: String,
    val subtotalPrice: Double,
    val shippingPrice: Double,
    val totalPrice: Double,
    val createdAt: String?,
    val address: AddressSnapshotResponse,
    val items: List<OrderItemResponse>,
    val shippingCarrier: String? = null,
    val trackingCode: String? = null,
    val shippedAt: String? = null,
    val deliveredAt: String? = null,
    val walletPaidAmount: Double? = null,
    val gatewayPaidAmount: Double? = null,
    val isGift: Boolean = false,
    val giftMessage: String? = null
)
