package com.kazemieh.network.cart.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class SetCartVariantQtyRequest(
    val qty: Int
)
