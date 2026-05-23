package com.kazemieh.network.dto.admin.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminUpdateVariantRequest(
    val sku: String? = null,
    val price: Double? = null,
    val compareAtPrice: Double? = null,
    val optionType: String? = null,
    val optionValue: String? = null,
    @SerialName("active")
    val isActive: Boolean? = null
)
