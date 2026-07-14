package com.kazemieh.domain.order

import com.kazemieh.domain.address.Address
import com.kazemieh.domain.cart.Cart

data class Order(
    val id: Long,
    val status: String,
    val subtotalPrice: Double,
    val shippingPrice: Double,
    val totalPrice: Double,
    val itemCount: Int = 0,
    val createdAt: String
)

data class ReorderResult(
    val cart: Cart,
    val skippedTitles: List<String>
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
    val deliveredAt: String?,
    val walletPaidAmount: Double? = null,
    val gatewayPaidAmount: Double? = null,
    val isGift: Boolean = false,
    val giftMessage: String? = null
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
    val shippedAt: String?,
    val history: List<OrderStatusHistoryItem> = emptyList()
)

data class OrderStatusHistoryItem(
    val status: String,
    val at: String
)
