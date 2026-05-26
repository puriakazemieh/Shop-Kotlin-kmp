package com.kazemieh.network.admin.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class AdminInventoryResponse(
    val variantId: Long,
    val onHand: Int,
    val reserved: Int,
    val available: Int = 0,
    val version: Int
)
