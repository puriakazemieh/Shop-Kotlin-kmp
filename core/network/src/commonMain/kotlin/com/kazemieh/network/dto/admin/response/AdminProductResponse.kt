package com.kazemieh.network.dto.admin.response

import kotlinx.serialization.Serializable

@Serializable
data class AdminProductResponse(
    val id: Long,
    val categoryId: Long?,
    val title: String,
    val slug: String,
    val description: String?,
    val basePrice: Double?,
    val isActive: Boolean
)
