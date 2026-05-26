package com.kazemieh.domain.cart

data class Cart(
    val items: List<CartItem>,
    val subtotal: Double,
    val totalQty: Int
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
    val isActive: Boolean,
    val lineTotal: Double
)
