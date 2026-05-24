package com.kazemieh.network.dto.admin.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminCreateVariantRequest(
    val options: List<AdminVariantOptionRequest>,
    val sku: String,
    val price: Double,
    val compareAtPrice: Double? = null,
    val isActive: Boolean = true,
    val initialOnHand: Int = 0
)

@Serializable
data class AdminVariantOptionRequest(
    val type: String,
    val value: String
)
