package com.kazemieh.network.admin.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminProductResponse(
    val id: Long = 0,
    val categoryId: Long? = null,
    val title: String = "",
    val slug: String = "",
    val description: String? = null,
    val basePrice: Double? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    @SerialName("active")
    val isActive: Boolean = true
)
