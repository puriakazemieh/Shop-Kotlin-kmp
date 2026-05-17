package com.kazemieh.network.dto.admin.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminVariantResponse(
    val id: Long,
    val sku: String,
    val price: Double,
    val compareAtPrice: Double? = null,
    @SerialName("active")
    val isActive: Boolean,
    val sizeId: Long, // اضافه شد
    val sizeName: String,
    val colorId: Long, // اضافه شد
    val colorName: String,
    val inventory: AdminInventoryResponse? = null
)
