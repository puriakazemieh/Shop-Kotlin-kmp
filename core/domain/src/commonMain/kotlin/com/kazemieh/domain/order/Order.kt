package com.kazemieh.domain.order

import com.kazemieh.domain.address.Address

data class Order(
    val id: Long,
    val status: String,
    val subtotalPrice: Double,
    val shippingPrice: Double,
    val totalPrice: Double,
    val createdAt: String
)

data class OrderDetail(
    val id: Long,
    val status: String,
    val subtotalPrice: Double,
    val shippingPrice: Double,
    val totalPrice: Double,
    val items: List<OrderItem>,
    val address: Address?,
    val createdAt: String,
    val shippingCarrier: String?,
    val trackingCode: String?,
    val shippedAt: String?,
    val deliveredAt: String?
)

data class OrderItem(
    val id: Long,
    val variantId: Long,
    val qty: Int,
    val unitPrice: Double,
    val title: String,
    val options: Map<String, String>
)

data class OrderTracking(
    val id: Long,
    val status: String,
    val trackingCode: String?,
    val orderedAt: String,
    val shippedAt: String?
)
