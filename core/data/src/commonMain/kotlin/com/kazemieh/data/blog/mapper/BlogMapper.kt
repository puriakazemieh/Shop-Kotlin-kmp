package com.kazemieh.data.blog.mapper

import com.kazemieh.domain.blog.Blog
import com.kazemieh.domain.blog.BlogBlock
import com.kazemieh.domain.blog.BlogCategory
import com.kazemieh.domain.blog.BlogList
import com.kazemieh.network.blog.dto.request.BlogBlockDto
import com.kazemieh.network.blog.dto.response.BlogCategoryResponse
import com.kazemieh.network.blog.dto.response.BlogListResponse
import com.kazemieh.network.blog.dto.response.BlogResponse

fun BlogResponse.toDomain(): Blog {
    val blocks = content?.map { block ->
        when (block.type) {
            "header" -> BlogBlock.Header(
                text = block.content,
                level = block.level ?: 1
            )
            "paragraph" -> BlogBlock.Paragraph(
                text = block.content
            )
            "image" -> BlogBlock.Image(
                url = block.content
            )
            else -> BlogBlock.Unknown(block.type)
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
        totalPages = page?.totalPages ?: totalPages,
        totalElements = page?.totalElements ?: totalElements
    )
}
