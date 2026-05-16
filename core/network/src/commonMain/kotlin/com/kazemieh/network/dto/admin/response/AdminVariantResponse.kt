package com.kazemieh.network.dto.admin.response

import kotlinx.serialization.Serializable

@Serializable
data class AdminVariantResponse(
    val id: Long,
    val sku: String,
    val price: Double,
    val compareAtPrice: Double?,
    val isActive: Boolean,
    val onHand: Int,
    val reserved: Int,
    val sizeName: String,
    val colorName: String
)
