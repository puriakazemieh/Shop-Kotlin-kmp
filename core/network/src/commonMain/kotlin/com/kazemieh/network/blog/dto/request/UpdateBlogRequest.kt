package com.kazemieh.network.blog.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateBlogRequest(
    val title: String? = null,
    val content: String? = null,
    val summary: String? = null,
    val thumbnailUrl: String? = null,
    val status: String? = null,
    val categoryId: Long? = null,
    val isFeatured: Boolean? = null,
    val metaTitle: String? = null,
    val metaDescription: String? = null
)
