package com.kazemieh.network.catalog.dto

import kotlinx.serialization.Serializable

@Serializable
data class PriceAlertRequestDto(
    val productId: Long,
    val variantId: Long,
    val targetPrice: Double
)
