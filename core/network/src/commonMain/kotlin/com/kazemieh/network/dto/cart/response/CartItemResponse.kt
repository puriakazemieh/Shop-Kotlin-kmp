package com.kazemieh.network.dto.cart.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CartItemResponse(
    val id: Long,
    val variantId: Long,
    val qty: Int,
    val productId: Long,
    val productTitle: String,
    val productSlug: String,
    val imageUrl: String? = null,
    val sizeName: String,
    val colorName: String,
    val price: Double,
    val compareAtPrice: Double? = null,
    val availableQty: Int,
    @SerialName("active")
    val isActive: Boolean,
    val lineTotal: Double
)
