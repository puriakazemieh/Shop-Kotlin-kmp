package com.kazemieh.network.cart.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateCartItemRequest(
    val qty: Int
)
