package com.kazemieh.network.dto.cart.request

import kotlinx.serialization.Serializable

@Serializable
data class AddCartItemRequest(
    val variantId: Long,
    val qty: Int
)
