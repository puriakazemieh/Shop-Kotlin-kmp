package com.kazemieh.network.dto.catalog.response

import kotlinx.serialization.Serializable

@Serializable
data class ProductVariantResponse(
    val id: Long,
    val sku: String,
    val price: Double,
    val compareAtPrice: Double?,
    val size: SizeResponse,
    val color: ColorResponse,
    val availableQty: Int,
    val active: Boolean
)
