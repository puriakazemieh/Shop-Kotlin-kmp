package com.kazemieh.network.blog.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateCategoryRequest(
    val name: String,
    val slug: String,
    val description: String? = null,
    val thumbnailUrl: String? = null
)

@Serializable
data class UpdateCategoryRequest(
    val name: String? = null,
    val slug: String? = null,
    val description: String? = null,
    val thumbnailUrl: String? = null
)
