package com.kazemieh.network.admin.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminCreateCategoryRequest(
    val name: String,
    val slug: String,
    val parentId: Long? = null
)
