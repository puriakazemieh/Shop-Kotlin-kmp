package com.kazemieh.network.dto.admin.response

import kotlinx.serialization.Serializable

@Serializable
data class AdminSizeResponse(
    val id: Long,
    val name: String,
    val sortOrder: Int
)
