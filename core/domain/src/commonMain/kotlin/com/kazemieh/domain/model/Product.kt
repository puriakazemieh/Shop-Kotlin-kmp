package com.kazemieh.domain.model

data class ProductSummary(
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

data class ProductDetail(
    val id: Long,
    val title: String,
    val slug: String,
    val description: String?,
    val categoryId: Long?,
    val categoryName: String?,
    val images: List<ProductImage>,
    val variants: List<ProductVariant>,
    val createdAt: String
)

data class ProductImage(
    val id: Long,
    val url: String,
    val sortOrder: Int
)

data class ProductVariant(
    val id: Long,
    val sku: String,
    val price: Double,
    val compareAtPrice: Double?,
    val available: Int,
    val sizeName: String,
    val colorName: String
)
