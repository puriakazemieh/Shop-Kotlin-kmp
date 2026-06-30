package com.kazemieh.domain.blog

import kotlinx.serialization.Serializable

data class Blog(
    val id: Long,
    val title: String,
    val slug: String,
    val summary: String? = null,
    val content: List<BlogBlock>? = null,
    val thumbnailUrl: String? = null,
    val viewCount: Int,
    val readingTimeMinutes: Int,
    val status: String? = null,
    val category: BlogCategory? = null,
    val categoryName: String? = null,
    val authorId: Long? = null,
    val authorName: String? = null,
    val isFeatured: Boolean = false,
    val metaTitle: String? = null,
    val metaDescription: String? = null,
    val createdAt: String,
    val updatedAt: String? = null
)

@Serializable
sealed interface BlogBlock {
    @Serializable
    data class Header(val text: String, val level: Int) : BlogBlock
    @Serializable
    data class Paragraph(val text: String) : BlogBlock
    @Serializable
    data class Image(val url: String, val caption: String? = null) : BlogBlock
    @Serializable
    data class Button(val text: String, val url: String) : BlogBlock
    @Serializable
    data class ListBlock(val items: List<String>) : BlogBlock
    @Serializable
    data class Quote(val text: String) : BlogBlock
    @Serializable
    data object Divider : BlogBlock
    @Serializable
    data class Unknown(val type: String) : BlogBlock
}

data class BlogList(
    val content: List<Blog>,
    val totalPages: Int,
    val totalElements: Int
)
