package com.kazemieh.network.story.dto

import kotlinx.serialization.Serializable

@Serializable
data class StoryResponse(
    val id: Long,
    val mediaUrl: String,
    val mediaType: String,
    val productId: Long? = null,
    val title: String? = null,
    val createdAt: String
)
