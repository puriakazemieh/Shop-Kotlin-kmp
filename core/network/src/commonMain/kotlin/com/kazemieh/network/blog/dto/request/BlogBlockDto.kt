package com.kazemieh.network.blog.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class BlogBlockDto(
    val type: String,    // "header", "paragraph", "image"
    val content: String, // text or image url
    val level: Int? = null // only for header (1, 2, 3)
)
