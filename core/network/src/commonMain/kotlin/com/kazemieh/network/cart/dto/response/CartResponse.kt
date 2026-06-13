package com.kazemieh.network.cart.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class CartResponse(
    val items: List<CartItemResponse> = emptyList(),
    val savedForLater: List<CartItemResponse> = emptyList(),
    val subtotal: Double = 0.0,
    val totalQty: Int = 0
)
