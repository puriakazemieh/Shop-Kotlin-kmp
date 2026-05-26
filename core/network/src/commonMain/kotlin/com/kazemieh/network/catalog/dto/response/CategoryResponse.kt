package com.kazemieh.network.catalog.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class CategoryResponse(
    val id: Long,
    val name: String,
    val slug: String,
    val parentId: Long? = null,
    val children: List<CategoryResponse> = emptyList()
)
