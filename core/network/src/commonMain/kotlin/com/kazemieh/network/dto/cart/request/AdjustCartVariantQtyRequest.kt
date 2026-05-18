package com.kazemieh.network.dto.cart.request

import kotlinx.serialization.Serializable

@Serializable
data class AdjustCartVariantQtyRequest(
    val delta: Int
)
