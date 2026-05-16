package com.kazemieh.network.dto.admin.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminVariantResponse(
    val id: Long,
    val sku: String,
    val price: Double,
    val compareAtPrice: Double? = null,
    @SerialName("active") val isActive: Boolean = true,
    val onHand: Int = 0,
    val reserved: Int = 0,
    val sizeName: String = "",
    val colorName: String = ""
)
