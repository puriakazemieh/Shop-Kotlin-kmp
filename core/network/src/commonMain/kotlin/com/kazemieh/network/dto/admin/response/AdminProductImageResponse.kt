package com.kazemieh.network.dto.admin.response

import kotlinx.serialization.Serializable

@Serializable
data class AdminProductImageResponse(
    val id: Long,
    val url: String,
    val sortOrder: Int
)
