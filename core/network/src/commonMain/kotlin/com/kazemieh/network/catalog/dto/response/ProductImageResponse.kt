package com.kazemieh.network.catalog.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ProductImageResponse(
    val id: Long? = null,
    val url: String,
    val sortOrder: Int
)
