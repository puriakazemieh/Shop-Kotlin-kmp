package com.kazemieh.network.admin.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class ReorderItem(
    val id: Long,
    val sortOrder: Int
)
