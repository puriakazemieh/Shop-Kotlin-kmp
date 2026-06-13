package com.kazemieh.network.cart.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CartItemResponse(
    val id: Long = 0,
    val variantId: Long = 0,
    val qty: Int = 0,
    val productId: Long = 0,
    val productTitle: String = "",
    val productSlug: String = "",
    val imageUrl: String? = null,
    val options: Map<String, String> = emptyMap(),
    val price: Double = 0.0,
    val compareAtPrice: Double? = null,
    val availableQty: Int = 0,
    val savedForLater: Boolean = false,
    @SerialName("active")
    val isActive: Boolean = true,
    val lineTotal: Double = 0.0
)
