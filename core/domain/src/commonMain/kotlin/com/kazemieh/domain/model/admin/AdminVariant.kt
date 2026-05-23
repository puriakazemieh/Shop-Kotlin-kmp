package com.kazemieh.domain.model.admin

data class AdminVariant(
    val id: Long,
    val sku: String,
    val price: Double,
    val compareAtPrice: Double?,
    val isActive: Boolean,
    val onHand: Int,
    val reserved: Int,
    val options: Map<String, String>
)

data class AdminCreateVariant(
    val options: Map<String, String>,
    val sku: String,
    val price: Double,
    val compareAtPrice: Double? = null,
    val isActive: Boolean = true,
    val initialOnHand: Int = 0
)
