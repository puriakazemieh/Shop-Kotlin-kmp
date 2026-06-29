package com.kazemieh.network.catalog.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class BannerResponse(
    val id: Long = 0,
    val title: String = "",
    val subtitle: String? = null,
    val imageUrl: String? = null,
    val categoryId: Long? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true
)
