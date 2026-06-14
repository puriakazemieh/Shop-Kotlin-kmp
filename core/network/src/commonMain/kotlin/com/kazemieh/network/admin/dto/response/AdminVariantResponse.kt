package com.kazemieh.network.admin.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminVariantResponse(
    val id: Long,
    val sku: String,
    val price: Double,
    val discountedPrice: Double? = null,
    val compareAtPrice: Double? = null,
    @SerialName("active")
    val isActive: Boolean,
    val options: Map<String, String>,
    val inventory: AdminInventoryResponse? = null
)
