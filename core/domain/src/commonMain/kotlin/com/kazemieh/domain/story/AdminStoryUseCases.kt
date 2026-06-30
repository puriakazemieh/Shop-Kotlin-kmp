package com.kazemieh.domain.story

import com.kazemieh.common.AppResult

class GetAdminStoriesUseCase(private val repository: StoryRepository) {
    suspend operator fun invoke(): AppResult<List<Story>> = repository.getAdminStories()
}

class CreateStoryUseCase(private val repository: StoryRepository) {
    suspend operator fun invoke(
        bytes: ByteArray,
        mediaType: StoryMediaType,
        productId: Long?,
        title: String?,
        linkType: StoryLinkType = StoryLinkType.NONE,
        categoryId: Long? = null,
        blogSlug: String? = null
    ): AppResult<Story> = repository.createStory(bytes, mediaType, productId, title, linkType, categoryId, blogSlug)
}

class UpdateStoryUseCase(private val repository: StoryRepository) {
    suspend operator fun invoke(
        id: Long,
        productId: Long?,
        title: String?,
        linkType: StoryLinkType? = null,
        categoryId: Long? = null,
        blogSlug: String? = null
    ): AppResult<Story> = repository.updateStory(id, productId, title, linkType, categoryId, blogSlug)
}

class DeleteStoryUseCase(private val repository: StoryRepository) {
    suspend operator fun invoke(id: Long): AppResult<Unit> = repository.deleteStory(id)
}
