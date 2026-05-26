package com.kazemieh.network.admin.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class AdminProductImageResponse(
    val id: Long,
    val url: String,
    val sortOrder: Int
)
