package com.kazemieh.network.dto.admin.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminReorderImagesRequest(
    val items: List<ReorderItem>
)
