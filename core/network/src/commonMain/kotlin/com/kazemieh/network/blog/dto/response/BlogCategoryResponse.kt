package com.kazemieh.network.blog.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class BlogCategoryResponse(
    val id: Long,
    val name: String,
    val slug: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val blogCount: Int = 0
)
