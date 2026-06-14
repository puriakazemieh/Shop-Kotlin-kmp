package com.kazemieh.network.admin.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class AdminProductVideoResponse(
    val id: Long,
    val url: String,
    val sortOrder: Int
)
