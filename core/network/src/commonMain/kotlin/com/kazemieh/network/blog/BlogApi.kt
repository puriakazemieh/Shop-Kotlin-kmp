package com.kazemieh.network.blog

import com.kazemieh.network.blog.dto.request.*
import com.kazemieh.network.blog.dto.response.*

interface BlogApi {
    suspend fun getBlogs(
        page: Int,
        size: Int,
        categoryId: Long? = null,
        searchQuery: String? = null
    ): BlogListResponse

    suspend fun getBlogDetail(slug: String): BlogResponse
    suspend fun getRelatedBlogs(slug: String): List<BlogResponse>
    suspend fun getCategories(): List<BlogCategoryResponse>

    // Admin APIs
    suspend fun getAdminBlogs(page: Int, size: Int): BlogListResponse
    suspend fun createBlog(request: CreateBlogRequest): BlogResponse
    suspend fun updateBlog(id: Long, request: UpdateBlogRequest): BlogResponse
    suspend fun deleteBlog(id: Long)
    suspend fun uploadMedia(fileBytes: ByteArray, fileName: String): UploadMediaResponse

    suspend fun createCategory(request: CreateCategoryRequest): BlogCategoryResponse
    suspend fun updateCategory(id: Long, request: UpdateCategoryRequest): BlogCategoryResponse
    suspend fun deleteCategory(id: Long)
}
