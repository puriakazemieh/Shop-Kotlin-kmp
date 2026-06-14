package com.kazemieh.network.admin.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminUpdateVariantRequest(
    val sku: String? = null,
    val price: Double? = null,
    val discountedPrice: Double? = null,
    val compareAtPrice: Double? = null,
    val options: List<AdminVariantOptionRequest>? = null,
    @SerialName("active")
    val isActive: Boolean? = null
)
