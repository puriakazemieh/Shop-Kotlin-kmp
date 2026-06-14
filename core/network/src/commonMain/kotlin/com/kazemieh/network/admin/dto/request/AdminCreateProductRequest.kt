package com.kazemieh.network.admin.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminCreateProductRequest(
    val categoryId: Long?,
    val title: String,
    val slug: String,
    val description: String?,
    val basePrice: Double?,
    val discountedPrice: Double? = null,
    val sku: String? = null,
    val initialOnHand: Int? = null,
    val isActive: Boolean,
    val variants: List<AdminCreateVariantRequest>? = null
)
