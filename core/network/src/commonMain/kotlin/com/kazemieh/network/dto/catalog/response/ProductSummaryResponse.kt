package com.kazemieh.network.dto.catalog.response

import kotlinx.serialization.Serializable

@Serializable
data class ProductSummaryResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val thumbnailUrl: String?,
    val minPrice: Double?,
    val maxPrice: Double?,
    val inStock: Boolean,
    val categoryId: Long?,
    val categoryName: String?
)
