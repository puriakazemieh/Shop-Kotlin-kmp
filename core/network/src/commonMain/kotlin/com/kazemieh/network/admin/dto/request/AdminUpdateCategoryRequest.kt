package com.kazemieh.network.admin.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminUpdateCategoryRequest(
    val name: String? = null,
    val slug: String? = null,
    val parentId: Long? = null
)
