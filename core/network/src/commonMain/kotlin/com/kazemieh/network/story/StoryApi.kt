package com.kazemieh.network.story

import com.kazemieh.network.story.dto.StoryResponse

interface StoryApi {
    suspend fun getStories(): List<StoryResponse>

    // Admin Operations
    suspend fun getAdminStories(): List<StoryResponse>
    suspend fun createStory(
        bytes: ByteArray,
        mediaType: String,
        productId: Long?,
        title: String?,
        linkType: String,
        categoryId: Long?,
        blogSlug: String?
    ): StoryResponse
    suspend fun updateStory(
        id: Long,
        productId: Long?,
        title: String?,
        linkType: String?,
        categoryId: Long?,
        blogSlug: String?
    ): StoryResponse
    suspend fun deleteStory(id: Long)
}
