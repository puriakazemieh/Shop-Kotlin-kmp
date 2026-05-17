package com.kazemieh.network.dto.admin.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminCreateVariantRequest(
    val sizeId: Long,
    val colorId: Long,
    val sku: String,
    val price: Double,
    val compareAtPrice: Double? = null,
    val isActive: Boolean = true,
    val initialOnHand: Int = 0
)
