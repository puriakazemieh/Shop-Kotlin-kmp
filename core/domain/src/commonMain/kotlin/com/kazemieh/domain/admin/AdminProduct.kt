package com.kazemieh.domain.admin

data class AdminProduct(
    val id: Long,
    val categoryId: Long?,
    val title: String,
    val slug: String,
    val description: String?,
    val basePrice: Double?,
    val discountedPrice: Double? = null,
    val sku: String? = null,
    val initialOnHand: Int? = null,
    val isActive: Boolean
)

data class AdminProductDetail(
    val product: AdminProduct,
    val images: List<AdminProductImage>,
    val videos: List<AdminProductVideo> = emptyList(),
    val variants: List<AdminVariant>
)

data class AdminProductImage(
    val id: Long,
    val url: String,
    val sortOrder: Int
)

data class AdminProductVideo(
    val id: Long,
    val url: String,
    val sortOrder: Int
)
