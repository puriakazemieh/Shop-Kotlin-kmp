package com.kazemieh.network.blog.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class BlogResponse(
    val id: Long,
    val title: String,
    val slug: String,
    val summary: String? = null,
    val content: String? = null,
    val thumbnailUrl: String? = null,
    val viewCount: Int,
    val readingTimeMinutes: Int,
    val authorName: String? = null,
    val author: AuthorResponse? = null,
    val categoryName: String? = null,
    val category: BlogCategoryResponse? = null,
    val isFeatured: Boolean = false,
    val status: String? = null,
    val metaTitle: String? = null,
    val metaDescription: String? = null,
    val createdAt: String,
    val updatedAt: String? = null
)

@Serializable
data class AuthorResponse(
    val id: Long,
    val name: String
)
