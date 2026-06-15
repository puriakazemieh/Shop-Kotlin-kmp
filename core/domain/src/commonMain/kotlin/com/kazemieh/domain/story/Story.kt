package com.kazemieh.domain.story

import com.kazemieh.common.AppResult
import kotlinx.coroutines.flow.Flow

enum class StoryMediaType {
    IMAGE, VIDEO
}

data class Story(
    val id: Long,
    val mediaUrl: String,
    val mediaType: StoryMediaType,
    val productId: Long?,
    val title: String?,
    val createdAt: String,
    val isSeen: Boolean = false
)

interface StoryRepository {
    fun getStories(): Flow<AppResult<List<Story>>>
    suspend fun markAsSeen(id: Long)

    // Admin Operations
    suspend fun getAdminStories(): AppResult<List<Story>>
    suspend fun createStory(
        bytes: ByteArray,
        mediaType: StoryMediaType,
        productId: Long?,
        title: String?
    ): AppResult<Story>

    suspend fun updateStory(
        id: Long,
        productId: Long?,
        title: String?
    ): AppResult<Story>

    suspend fun deleteStory(id: Long): AppResult<Unit>
}

class GetStoriesUseCase(private val repository: StoryRepository) {
    operator fun invoke(): Flow<AppResult<List<Story>>> = repository.getStories()
}

class MarkStoryAsSeenUseCase(private val repository: StoryRepository) {
    suspend operator fun invoke(id: Long) = repository.markAsSeen(id)
}
