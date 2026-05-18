package com.kazemieh.network.dto.catalog.response

import kotlinx.serialization.Serializable

@Serializable
data class ProductImageResponse(
    val id: Long? = null,
    val url: String,
    val sortOrder: Int
)
