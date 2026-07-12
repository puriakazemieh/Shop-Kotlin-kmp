package com.kazemieh.network.order.dto.response

import com.kazemieh.network.cart.dto.response.CartResponse
import kotlinx.serialization.Serializable

@Serializable
data class ReorderResponse(
    val cart: CartResponse,
    val skippedTitles: List<String> = emptyList()
)
