package com.kazemieh.network.dto.admin.response

import kotlinx.serialization.Serializable

@Serializable
data class AdminCategoryResponse(
    val id: Long,
    val name: String = "",
    val slug: String = "",
    val parentId: Long? = null
)
