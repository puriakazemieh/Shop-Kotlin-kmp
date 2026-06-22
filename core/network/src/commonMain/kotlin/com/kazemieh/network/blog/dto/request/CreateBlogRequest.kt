package com.kazemieh.network.blog.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateBlogRequest(
    val title: String,
    val content: List<BlogBlockDto> = emptyList(),
    val summary: String? = null,
    val thumbnailUrl: String? = null,
    val status: String = "PUBLISHED",
    val categoryId: Long? = null,
    val isFeatured: Boolean = false,
    val metaTitle: String? = null,
    val metaDescription: String? = null
)
