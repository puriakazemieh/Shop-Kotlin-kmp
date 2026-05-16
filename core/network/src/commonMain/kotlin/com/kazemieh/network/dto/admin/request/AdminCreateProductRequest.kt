package com.kazemieh.network.dto.admin.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminCreateProductRequest(
    val categoryId: Long?,
    val title: String,
    val slug: String,
    val description: String?,
    val basePrice: Double?,
    val isActive: Boolean
)
