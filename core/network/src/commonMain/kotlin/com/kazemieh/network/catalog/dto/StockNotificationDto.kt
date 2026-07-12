package com.kazemieh.network.catalog.dto

import kotlinx.serialization.Serializable

@Serializable
data class StockNotificationRequestDto(
    val productId: Long,
    val variantId: Long
)
