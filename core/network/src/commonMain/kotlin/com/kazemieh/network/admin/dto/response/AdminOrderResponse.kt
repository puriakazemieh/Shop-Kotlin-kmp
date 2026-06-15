package com.kazemieh.network.admin.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class AdminOrderSummaryResponse(
    val id: Long = 0,
    val userId: Long = 0,
    val userEmail: String = "",
    val status: String = "",
    val totalPrice: Double = 0.0,
    val createdAt: String? = null
)

@Serializable
data class AdminOrderDetailResponse(
    val id: Long = 0,
    val userId: Long = 0,
    val userEmail: String = "",
    val status: String = "",
    val subtotalPrice: Double = 0.0,
    val shippingPrice: Double = 0.0,
    val totalPrice: Double = 0.0,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val addressSnapshot: AdminAddressSnapshotResponse = AdminAddressSnapshotResponse(),
    val items: List<AdminOrderItemResponse> = emptyList(),
    val walletPaidAmount: Double? = null,
    val gatewayPaidAmount: Double? = null
)

@Serializable
data class AdminOrderItemResponse(
    val id: Long? = null,
    val variantId: Long = 0,
    val qty: Int = 0,
    val unitPriceSnapshot: Double = 0.0,
    val titleSnapshot: String = "",
    val optionsSnapshot: Map<String, String> = emptyMap()
)

@Serializable
data class AdminAddressSnapshotResponse(
    val receiverName: String = "",
    val receiverPhone: String = "",
    val country: String = "",
    val province: String = "",
    val city: String = "",
    val addressLine1: String = "",
    val addressLine2: String? = null,
    val postalCode: String? = null
)
