package com.kazemieh.network.admin.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminReorderImagesRequest(
    val items: List<ReorderItem>
)
