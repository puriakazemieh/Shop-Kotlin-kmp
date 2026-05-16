package com.kazemieh.network.dto.admin.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminAddImageRequest(
    val url: String,
    val sortOrder: Int? = null
)
