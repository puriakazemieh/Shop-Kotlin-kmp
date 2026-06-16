package com.kazemieh.data.blog.source

import com.kazemieh.network.blog.BlogApi
import com.kazemieh.network.blog.dto.request.*
import com.kazemieh.network.blog.dto.response.*

class BlogDataSourceImpl(
    private val api: BlogApi
) : BlogDataSource {
    override suspend fun getBlogs(
        page: Int,
        size: Int,
        categoryId: Long?,
        searchQuery: String?
    ): BlogListResponse = api.getBlogs(page, size, categoryId, searchQuery)

    override suspend fun getBlogDetail(slug: String): BlogResponse = api.getBlogDetail(slug)
    override suspend fun getRelatedBlogs(slug: String): List<BlogResponse> = api.getRelatedBlogs(slug)
    override suspend fun getCategories(): List<BlogCategoryResponse> = api.getCategories()

    override suspend fun getAdminBlogs(page: Int, size: Int): BlogListResponse = api.getAdminBlogs(page, size)
    override suspend fun createBlog(request: CreateBlogRequest): BlogResponse = api.createBlog(request)
    override suspend fun updateBlog(id: Long, request: UpdateBlogRequest): BlogResponse = api.updateBlog(id, request)
    override suspend fun deleteBlog(id: Long) = api.deleteBlog(id)
    override suspend fun uploadMedia(fileBytes: ByteArray, fileName: String): UploadMediaResponse =
        api.uploadMedia(fileBytes, fileName)

    override suspend fun createCategory(request: CreateCategoryRequest): BlogCategoryResponse =
        api.createCategory(request)

    override suspend fun updateCategory(id: Long, request: UpdateCategoryRequest): BlogCategoryResponse =
        api.updateCategory(id, request)

    override suspend fun deleteCategory(id: Long) = api.deleteCategory(id)
}
