package com.kazemieh.network.dto.cart.request

import kotlinx.serialization.Serializable

@Serializable
data class SetCartVariantQtyRequest(
    val qty: Int
)
