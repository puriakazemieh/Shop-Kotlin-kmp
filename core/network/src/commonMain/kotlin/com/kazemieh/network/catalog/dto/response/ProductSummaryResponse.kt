package com.kazemieh.network.catalog.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ProductSummaryResponse(
    val id: Long = 0,
    val title: String = "",
    val slug: String = "",
    val thumbnailUrl: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val minDiscountedPrice: Double? = null,
    val maxDiscountedPrice: Double? = null,
    val inStock: Boolean = false,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val options: Map<String, List<String>> = emptyMap(),
    val isFavorite: Boolean = false,
    val averageRating: Double? = null,
    val reviewCount: Long = 0
)
