package com.kazemieh.network.cart.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class ApplyDiscountRequest(
    val code: String
)
