package com.kazemieh.network.dto.admin.request

import kotlinx.serialization.Serializable

@Serializable
data class ReorderItem(
    val id: Long,
    val sortOrder: Int
)
