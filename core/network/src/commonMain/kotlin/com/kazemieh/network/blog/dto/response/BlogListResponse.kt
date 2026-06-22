package com.kazemieh.network.blog.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class BlogListResponse(
    val content: List<BlogResponse>,
    val page: PageResponse? = null,
    // Keep for backward compatibility if other APIs use them at root
    val totalPages: Int = 0,
    val totalElements: Int = 0
)

@Serializable
data class PageResponse(
    val size: Int,
    val number: Int,
    val totalElements: Int,
    val totalPages: Int
)
