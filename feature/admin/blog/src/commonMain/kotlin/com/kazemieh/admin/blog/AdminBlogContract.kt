package com.kazemieh.admin.blog

import com.kazemieh.domain.blog.Blog
import com.kazemieh.domain.blog.BlogCategory

data class AdminBlogListState(
    val isLoading: Boolean = false,
    val blogs: List<Blog> = emptyList(),
    val categories: List<BlogCategory> = emptyList(),
    val error: Any? = null
)

sealed interface AdminBlogListIntent {
    data object LoadBlogs : AdminBlogListIntent
    data object LoadCategories : AdminBlogListIntent
    data class DeleteBlog(val id: Long) : AdminBlogListIntent
    data class DeleteCategory(val id: Long) : AdminBlogListIntent
}

sealed interface AdminBlogListEffect {
    data class ShowError(val message: Any) : AdminBlogListEffect
    data object BlogDeleted : AdminBlogListEffect
    data object CategoryDeleted : AdminBlogListEffect
}
