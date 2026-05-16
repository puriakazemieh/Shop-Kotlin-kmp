package com.kazemieh.domain.model.admin

data class AdminProduct(
    val id: Long,
    val categoryId: Long?,
    val title: String,
    val slug: String,
    val description: String?,
    val basePrice: Double?,
    val isActive: Boolean
)

data class AdminProductDetail(
    val product: AdminProduct,
    val images: List<AdminProductImage>,
    val variants: List<AdminVariant>
)

data class AdminProductImage(
    val id: Long,
    val url: String,
    val sortOrder: Int
)

data class AdminVariant(
    val id: Long,
    val sku: String,
    val price: Double,
    val compareAtPrice: Double?,
    val isActive: Boolean,
    val onHand: Int,
    val reserved: Int,
    val sizeName: String,
    val colorName: String
)
