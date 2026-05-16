package com.kazemieh.network.dto.admin.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminUpdateVariantRequest(
    val sku: String? = null,
    val price: Double? = null,
    val compareAtPrice: Double? = null,
    val isActive: Boolean? = null
)
