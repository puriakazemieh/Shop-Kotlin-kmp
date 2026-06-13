package com.kazemieh.network.catalog.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ProductVariantResponse(
    val id: Long = 0,
    val sku: String = "",
    val price: Double = 0.0,
    val compareAtPrice: Double? = null,
    val options: Map<String, String> = emptyMap(),
    val availableQty: Int = 0,
    val active: Boolean = false
)
