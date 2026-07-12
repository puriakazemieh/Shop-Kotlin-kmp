package com.kazemieh.domain.admin

data class AdminProduct(
    val id: Long,
    val categoryId: Long?,
    val title: String,
    val slug: String,
    val description: String?,
    val brand: String? = null,
    val attributes: List<com.kazemieh.domain.catalog.ProductAttribute> = emptyList(),
    val basePrice: Double?,
    val discountedPrice: Double? = null,
    val sku: String? = null,
    val initialOnHand: Int? = null,
    val categoryName: String? = null,
    val thumbnailUrl: String? = null,
    val stock: Int = 0,
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
