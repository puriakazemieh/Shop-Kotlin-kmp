package com.kazemieh.network.cart.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class AddCartItemRequest(
    val variantId: Long,
    val qty: Int
)
