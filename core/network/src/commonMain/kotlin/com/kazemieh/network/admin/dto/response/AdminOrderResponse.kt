package com.kazemieh.network.admin.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class AdminOrderSummaryResponse(
    val id: Long,
    val userId: Long,
    val userEmail: String,
    val status: String,
    val totalPrice: Double,
    val createdAt: String?
)

@Serializable
data class AdminOrderDetailResponse(
    val id: Long,
    val userId: Long,
    val userEmail: String,
    val status: String,
    val subtotalPrice: Double,
    val shippingPrice: Double,
    val totalPrice: Double,
    val createdAt: String?,
    val updatedAt: String?,
    val addressSnapshot: AdminAddressSnapshotResponse,
    val items: List<AdminOrderItemResponse>
)

@Serializable
data class AdminOrderItemResponse(
    val id: Long? = null,
    val variantId: Long,
    val qty: Int,
    val unitPriceSnapshot: Double,
    val titleSnapshot: String,
    val optionsSnapshot: Map<String, String>
)

@Serializable
data class AdminAddressSnapshotResponse(
    val receiverName: String,
    val receiverPhone: String,
    val country: String,
    val province: String,
    val city: String,
    val addressLine1: String,
    val addressLine2: String?,
    val postalCode: String?
)
