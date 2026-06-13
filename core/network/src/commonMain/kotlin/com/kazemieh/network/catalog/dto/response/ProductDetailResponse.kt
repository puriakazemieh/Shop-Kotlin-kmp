package com.kazemieh.network.catalog.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ProductDetailResponse(
    val id: Long = 0,
    val title: String = "",
    val slug: String = "",
    val description: String? = null,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val images: List<ProductImageResponse> = emptyList(),
    val variants: List<ProductVariantResponse> = emptyList(),
    val createdAt: String = "",
    val basePrice: Double? = null,
    val discountedPrice: Double? = null,
)
