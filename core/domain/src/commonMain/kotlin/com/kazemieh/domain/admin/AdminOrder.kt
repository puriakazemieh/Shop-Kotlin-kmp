package com.kazemieh.domain.admin

data class AdminOrderSummary(
    val id: Long,
    val userId: Long,
    val userEmail: String,
    val status: String,
    val totalPrice: Double,
    val createdAt: String?
)

data class AdminOrderDetail(
    val id: Long,
    val userId: Long,
    val userEmail: String,
    val status: String,
    val subtotalPrice: Double,
    val shippingPrice: Double,
    val totalPrice: Double,
    val createdAt: String?,
    val updatedAt: String?,
    val addressSnapshot: AdminAddressSnapshot,
    val items: List<AdminOrderItem>
)

data class AdminOrderItem(
    val id: Long,
    val variantId: Long,
    val qty: Int,
    val unitPriceSnapshot: Double,
    val titleSnapshot: String,
    val optionsSnapshot: Map<String, String>
)

data class AdminAddressSnapshot(
    val receiverName: String,
    val receiverPhone: String,
    val country: String,
    val province: String,
    val city: String,
    val addressLine1: String,
    val addressLine2: String?,
    val postalCode: String?
)
