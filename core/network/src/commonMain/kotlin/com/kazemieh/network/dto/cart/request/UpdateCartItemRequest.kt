package com.kazemieh.network.dto.cart.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateCartItemRequest(
    val qty: Int
)
