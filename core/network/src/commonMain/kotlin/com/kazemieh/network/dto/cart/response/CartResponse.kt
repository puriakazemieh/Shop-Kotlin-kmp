package com.kazemieh.network.dto.cart.response

import kotlinx.serialization.Serializable

@Serializable
data class CartResponse(
    val items: List<CartItemResponse>,
    val subtotal: Double,
    val totalQty: Int
)
