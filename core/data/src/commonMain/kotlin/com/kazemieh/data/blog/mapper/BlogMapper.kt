package com.kazemieh.data.blog.mapper

import com.kazemieh.domain.blog.Blog
import com.kazemieh.domain.blog.BlogBlock
import com.kazemieh.domain.blog.BlogCategory
import com.kazemieh.domain.blog.BlogList
import com.kazemieh.network.blog.dto.response.BlogCategoryResponse
import com.kazemieh.network.blog.dto.response.BlogListResponse
import com.kazemieh.network.blog.dto.response.BlogResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

private val json = Json { ignoreUnknownKeys = true }

@Serializable
private data class ContentBlockDto(
    val type: String,
    val data: JsonObject
)

@Serializable
private data class ContentBlocksDto(val blocks: List<ContentBlockDto>)

fun BlogResponse.toDomain(): Blog {
    val blocks = content?.let {
        try {
            val dto = json.decodeFromString<ContentBlocksDto>(it)
            dto.blocks.map { block ->
                when (block.type) {
                    "header" -> BlogBlock.Header(
                        text = block.data["text"]?.jsonPrimitive?.content ?: "",
                        level = block.data["level"]?.jsonPrimitive?.intOrNull ?: 1
                    )
                    "paragraph" -> BlogBlock.Paragraph(
                        text = block.data["text"]?.jsonPrimitive?.content ?: ""
                    )
                    "image" -> BlogBlock.Image(
                        url = block.data["url"]?.jsonPrimitive?.content ?: "",
                        caption = block.data["caption"]?.jsonPrimitive?.contentOrNull
                    )
                    else -> BlogBlock.Unknown(block.type)
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    return Blog(
        id = id,
        title = title,
        slug = slug,
        summary = summary,
        content = blocks,
        thumbnailUrl = thumbnailUrl,
        viewCount = viewCount,
        readingTimeMinutes = readingTimeMinutes,
        status = status,
        category = category?.toDomain(),
        categoryName = categoryName,
        authorId = author?.id,
        authorName = authorName ?: author?.name,
        isFeatured = isFeatured,
        metaTitle = metaTitle,
        metaDescription = metaDescription,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun BlogCategoryResponse.toDomain(): BlogCategory {
    return BlogCategory(
        id = id,
        name = name,
        slug = slug,
        description = description,
        thumbnailUrl = thumbnailUrl,
        blogCount = blogCount
    )
}

fun BlogListResponse.toDomain(): BlogList {
    return BlogList(
        content = content.map { it.toDomain() },
        totalPages = totalPages,
        totalElements = totalElements
    )
}
