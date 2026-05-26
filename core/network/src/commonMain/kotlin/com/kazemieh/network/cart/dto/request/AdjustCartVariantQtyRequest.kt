package com.kazemieh.network.cart.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class AdjustCartVariantQtyRequest(
    val delta: Int
)
