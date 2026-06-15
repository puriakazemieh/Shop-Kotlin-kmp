package com.kazemieh.data.story.source

import com.kazemieh.common.AppResult
import com.kazemieh.network.story.dto.StoryResponse

interface StoryDataSource {
    suspend fun getStories(): AppResult<List<StoryResponse>>
    suspend fun getAdminStories(): AppResult<List<StoryResponse>>
    suspend fun createStory(
        bytes: ByteArray,
        mediaType: String,
        productId: Long?,
        title: String?
    ): AppResult<StoryResponse>

    suspend fun updateStory(
        id: Long,
        productId: Long?,
        title: String?
    ): AppResult<StoryResponse>

    suspend fun deleteStory(id: Long): AppResult<Unit>
}
