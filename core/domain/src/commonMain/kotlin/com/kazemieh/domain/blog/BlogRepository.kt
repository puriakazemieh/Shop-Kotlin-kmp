package com.kazemieh.domain.blog

import com.kazemieh.common.AppResult

interface BlogRepository {
    suspend fun getBlogs(
        page: Int, 
        size: Int,
        categoryId: Long? = null,
        searchQuery: String? = null
    ): AppResult<BlogList>
    suspend fun getBlogDetail(slug: String): AppResult<Blog>
    suspend fun getRelatedBlogs(slug: String): AppResult<List<Blog>>
    suspend fun getCategories(): AppResult<List<BlogCategory>>
    
    // Admin
    suspend fun getAdminBlogs(page: Int, size: Int): AppResult<BlogList>
    suspend fun createBlog(blog: Blog): AppResult<Blog>
    suspend fun updateBlog(id: Long, blog: Blog): AppResult<Blog>
    suspend fun deleteBlog(id: Long): AppResult<Unit>
    suspend fun uploadMedia(fileBytes: ByteArray, fileName: String): AppResult<String>

    suspend fun createCategory(category: BlogCategory): AppResult<BlogCategory>
    suspend fun updateCategory(id: Long, category: BlogCategory): AppResult<BlogCategory>
    suspend fun deleteCategory(id: Long): AppResult<Unit>
}
