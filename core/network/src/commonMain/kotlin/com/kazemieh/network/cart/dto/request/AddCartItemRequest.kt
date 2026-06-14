package com.kazemieh.network.cart.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class AddCartItemRequest(
    val productId: Long? = null,
    val variantId: Long? = null,
    val qty: Int
)
