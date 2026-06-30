package com.kazemieh.network.catalog.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ProductDetailResponse(
    val id: Long = 0,
    val title: String = "",
    val slug: String = "",
    val description: String? = null,
    val brand: String? = null,
    val attributes: List<ProductAttributeDto> = emptyList(),
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val images: List<ProductImageResponse> = emptyList(),
    val videos: List<ProductVideoResponse> = emptyList(),
    val variants: List<ProductVariantResponse> = emptyList(),
    val createdAt: String = "",
    val basePrice: Double? = null,
    val discountedPrice: Double? = null,
    val isFavorite: Boolean = false
)
