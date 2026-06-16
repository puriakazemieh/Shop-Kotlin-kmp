package com.kazemieh.data.blog.source

import com.kazemieh.network.blog.dto.request.*
import com.kazemieh.network.blog.dto.response.*

interface BlogDataSource {
    suspend fun getBlogs(
        page: Int,
        size: Int,
        categoryId: Long? = null,
        searchQuery: String? = null
    ): BlogListResponse

    suspend fun getBlogDetail(slug: String): BlogResponse
    suspend fun getRelatedBlogs(slug: String): List<BlogResponse>
    suspend fun getCategories(): List<BlogCategoryResponse>

    suspend fun getAdminBlogs(page: Int, size: Int): BlogListResponse
    suspend fun createBlog(request: CreateBlogRequest): BlogResponse
    suspend fun updateBlog(id: Long, request: UpdateBlogRequest): BlogResponse
    suspend fun deleteBlog(id: Long)
    suspend fun uploadMedia(fileBytes: ByteArray, fileName: String): UploadMediaResponse

    suspend fun createCategory(request: CreateCategoryRequest): BlogCategoryResponse
    suspend fun updateCategory(id: Long, request: UpdateCategoryRequest): BlogCategoryResponse
    suspend fun deleteCategory(id: Long)
}
