package com.kazemieh.network.dto.catalog.response

import kotlinx.serialization.Serializable

@Serializable
data class ProductVariantResponse(
    val id: Long,
    val sku: String,
    val price: Double,
    val compareAtPrice: Double?,
    val options: Map<String, String>,
    val availableQty: Int,
    val active: Boolean
)
