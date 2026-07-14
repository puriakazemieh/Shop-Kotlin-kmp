package com.kazemieh.domain.cart

data class Cart(
    val items: List<CartItem>,
    val savedForLater: List<CartItem> = emptyList(),
    val subtotal: Double,
    val totalQty: Int,
    val discountAmount: Double = 0.0,
    val total: Double = subtotal,
    val appliedDiscountCode: String? = null,
    val updatedAt: String? = null
)

data class CartItem(
    val id: Long,
    val variantId: Long,
    val qty: Int,
    val productId: Long,
    val productTitle: String,
    val productSlug: String,
    val imageUrl: String?,
    val options: Map<String, String>,
    val price: Double,
    val compareAtPrice: Double?,
    val availableQty: Int,
    val savedForLater: Boolean = false,
    val isActive: Boolean,
    val lineTotal: Double
)
