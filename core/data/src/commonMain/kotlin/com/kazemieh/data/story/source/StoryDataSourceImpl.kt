package com.kazemieh.data.story.source

import com.kazemieh.network.story.StoryApi
import com.kazemieh.network.story.dto.StoryResponse
import com.kazemieh.network.common.safeApiCall
import com.kazemieh.common.AppResult

class StoryDataSourceImpl(
    private val api: StoryApi
) : StoryDataSource {
    override suspend fun getStories(): AppResult<List<StoryResponse>> = safeApiCall {
        api.getStories()
    }

    override suspend fun getAdminStories(): AppResult<List<StoryResponse>> = safeApiCall {
        api.getAdminStories()
    }

    override suspend fun createStory(
        bytes: ByteArray,
        mediaType: String,
        productId: Long?,
        title: String?
    ): AppResult<StoryResponse> = safeApiCall {
        api.createStory(bytes, mediaType, productId, title)
    }

    override suspend fun updateStory(
        id: Long,
        productId: Long?,
        title: String?
    ): AppResult<StoryResponse> = safeApiCall {
        api.updateStory(id, productId, title)
    }

    override suspend fun deleteStory(id: Long): AppResult<Unit> = safeApiCall {
        api.deleteStory(id)
    }
}
