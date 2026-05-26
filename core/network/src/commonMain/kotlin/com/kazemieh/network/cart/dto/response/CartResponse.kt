package com.kazemieh.network.cart.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class CartResponse(
    val items: List<CartItemResponse>,
    val subtotal: Double,
    val totalQty: Int
)
