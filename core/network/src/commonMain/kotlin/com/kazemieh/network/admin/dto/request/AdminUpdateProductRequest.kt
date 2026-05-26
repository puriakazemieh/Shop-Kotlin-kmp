package com.kazemieh.network.admin.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminUpdateProductRequest(
    val categoryId: Long? = null,
    val title: String? = null,
    val slug: String? = null,
    val description: String? = null,
    val basePrice: Double? = null,
    val isActive: Boolean? = null
)
