package com.kazemieh.network.admin.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminAddImageRequest(
    val url: String,
    val sortOrder: Int? = null
)
