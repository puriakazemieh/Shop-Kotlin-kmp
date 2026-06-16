package com.kazemieh.data.blog.repository

import com.kazemieh.common.AppResult
import com.kazemieh.data.blog.mapper.toDomain
import com.kazemieh.data.blog.source.BlogDataSource
import com.kazemieh.domain.blog.Blog
import com.kazemieh.domain.blog.BlogCategory
import com.kazemieh.domain.blog.BlogList
import com.kazemieh.domain.blog.BlogRepository
import com.kazemieh.network.blog.dto.request.*
import com.kazemieh.network.common.safeApiCall
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class BlogRepositoryImpl(
    private val dataSource: BlogDataSource
) : BlogRepository {
    override suspend fun getBlogs(
        page: Int,
        size: Int,
        categoryId: Long?,
        searchQuery: String?
    ): AppResult<BlogList> = safeApiCall {
        dataSource.getBlogs(page, size, categoryId, searchQuery).toDomain()
    }

    override suspend fun getFeaturedBlogs(): AppResult<List<Blog>> = safeApiCall {
        dataSource.getFeaturedBlogs().map { it.toDomain() }
    }

    override suspend fun getBlogDetail(slug: String): AppResult<Blog> = safeApiCall {
        dataSource.getBlogDetail(slug).toDomain()
    }

    override suspend fun getRelatedBlogs(slug: String): AppResult<List<Blog>> = safeApiCall {
        dataSource.getRelatedBlogs(slug).map { it.toDomain() }
    }

    override suspend fun getCategories(): AppResult<List<BlogCategory>> = safeApiCall {
        dataSource.getCategories().map { it.toDomain() }
    }

    override suspend fun getAdminBlogs(page: Int, size: Int): AppResult<BlogList> = safeApiCall {
        dataSource.getAdminBlogs(page, size).toDomain()
    }

    override suspend fun getAdminBlogDetail(slug: String): AppResult<Blog> = safeApiCall {
        dataSource.getAdminBlogDetail(slug).toDomain()
    }

    override suspend fun createBlog(blog: Blog): AppResult<Blog> = safeApiCall {
        dataSource.createBlog(
            CreateBlogRequest(
                title = blog.title,
                content = blog.content?.let { Json.encodeToString(it) } ?: "",
                summary = blog.summary,
                thumbnailUrl = blog.thumbnailUrl,
                status = blog.status ?: "PUBLISHED",
                categoryId = blog.category?.id,
                isFeatured = blog.isFeatured,
                metaTitle = blog.metaTitle,
                metaDescription = blog.metaDescription
            )
        ).toDomain()
    }

    override suspend fun updateBlog(id: Long, blog: Blog): AppResult<Blog> = safeApiCall {
        dataSource.updateBlog(
            id,
            UpdateBlogRequest(
                title = blog.title,
                content = blog.content?.let { Json.encodeToString(it) },
                summary = blog.summary,
                thumbnailUrl = blog.thumbnailUrl,
                status = blog.status,
                categoryId = blog.category?.id,
                isFeatured = blog.isFeatured,
                metaTitle = blog.metaTitle,
                metaDescription = blog.metaDescription
            )
        ).toDomain()
    }

    override suspend fun deleteBlog(id: Long): AppResult<Unit> = safeApiCall {
        dataSource.deleteBlog(id)
    }

    override suspend fun uploadMedia(fileBytes: ByteArray, fileName: String): AppResult<String> = safeApiCall {
        dataSource.uploadMedia(fileBytes, fileName).url
    }

    override suspend fun createCategory(category: BlogCategory): AppResult<BlogCategory> = safeApiCall {
        dataSource.createCategory(
            CreateCategoryRequest(
                name = category.name,
                slug = category.slug,
                description = category.description,
                thumbnailUrl = category.thumbnailUrl
            )
        ).toDomain()
    }

    override suspend fun updateCategory(id: Long, category: BlogCategory): AppResult<BlogCategory> = safeApiCall {
        dataSource.updateCategory(
            id,
            UpdateCategoryRequest(
                name = category.name,
                slug = category.slug,
                description = category.description,
                thumbnailUrl = category.thumbnailUrl
            )
        ).toDomain()
    }

    override suspend fun deleteCategory(id: Long): AppResult<Unit> = safeApiCall {
        dataSource.deleteCategory(id)
    }
}
