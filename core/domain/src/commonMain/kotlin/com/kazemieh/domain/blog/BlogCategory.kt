package com.kazemieh.domain.blog

data class BlogCategory(
    val id: Long,
    val name: String,
    val slug: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val blogCount: Int = 0
)
