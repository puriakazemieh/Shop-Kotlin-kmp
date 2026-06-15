package com.kazemieh.data.story.mapper

import com.kazemieh.domain.story.Story
import com.kazemieh.domain.story.StoryMediaType
import com.kazemieh.network.story.dto.StoryResponse

fun StoryResponse.toDomain(isSeen: Boolean): Story {
    return Story(
        id = id,
        mediaUrl = mediaUrl,
        mediaType = StoryMediaType.valueOf(mediaType.uppercase()),
        productId = productId,
        title = title,
        createdAt = createdAt,
        isSeen = isSeen
    )
}
