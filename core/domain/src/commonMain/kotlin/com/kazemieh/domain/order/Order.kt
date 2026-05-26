package com.kazemieh.domain.order

data class Order(
    val id: Long,
    val status: String,
    val totalPrice: Double,
    val createdAt: String,
    val itemCount: Int
)

data class OrderDetail(
    val id: Long,
    val status: String,
    val subtotalPrice: Double,
    val shippingPrice: Double,
    val totalPrice: Double,
    val items: List<OrderItem>,
    val address: String?,
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
