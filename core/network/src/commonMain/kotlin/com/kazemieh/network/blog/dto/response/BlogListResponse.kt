package com.kazemieh.network.blog.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class BlogListResponse(
    val content: List<BlogResponse>,
    val totalPages: Int,
    val totalElements: Int
)
