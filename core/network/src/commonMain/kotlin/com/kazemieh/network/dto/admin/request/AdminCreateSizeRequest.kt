package com.kazemieh.network.dto.admin.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminCreateSizeRequest(
    val name: String,
    val sortOrder: Int
)
