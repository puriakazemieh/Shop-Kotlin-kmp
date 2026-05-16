package com.kazemieh.network.dto.catalog.response

import kotlinx.serialization.Serializable

@Serializable
data class ProductDetailResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String?,
    val categoryId: Long?,
    val categoryName: String?,
    val images: List<ProductImageResponse>,
    val variants: List<ProductVariantResponse>,
    val createdAt: String
)
