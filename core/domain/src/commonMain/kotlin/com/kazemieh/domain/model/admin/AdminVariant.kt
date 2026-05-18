package com.kazemieh.domain.model.admin

data class AdminVariant(
    val id: Long,
    val sku: String,
    val price: Double,
    val compareAtPrice: Double?,
    val isActive: Boolean,
    val onHand: Int,
    val reserved: Int,
    val sizeId: Long, // اضافه شد
    val sizeName: String,
    val colorId: Long, // اضافه شد
    val colorName: String
)

data class AdminCreateVariant(
    val sizeId: Long,
    val colorId: Long,
    val sku: String,
    val price: Double,
    val compareAtPrice: Double? = null,
    val isActive: Boolean = true,
    val initialOnHand: Int = 0
)
