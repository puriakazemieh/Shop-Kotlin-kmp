package com.kazemieh.blog

import com.kazemieh.domain.blog.Blog
import com.kazemieh.domain.blog.BlogCategory
import com.kazemieh.domain.blog.BlogList

data class BlogListState(
    val isLoading: Boolean = false,
    val blogs: List<Blog> = emptyList(),
    val featuredBlogs: List<Blog> = emptyList(),
    val categories: List<BlogCategory> = emptyList(),
    val selectedCategoryId: Long? = null,
    val searchQuery: String = "",
    val error: Any? = null
)

sealed interface BlogListIntent {
    data object LoadBlogs : BlogListIntent
    data object LoadFeaturedBlogs : BlogListIntent
    data object LoadCategories : BlogListIntent
    data class SelectCategory(val categoryId: Long?) : BlogListIntent
    data class Search(val query: String) : BlogListIntent
    data object Refresh : BlogListIntent
}

sealed interface BlogListEffect {
    data class ShowError(val message: Any) : BlogListEffect
}

data class BlogDetailState(
    val isLoading: Boolean = false,
    val blog: Blog? = null,
    val relatedBlogs: List<Blog> = emptyList(),
    val error: Any? = null
)

sealed interface BlogDetailIntent {
    data class LoadBlog(val slug: String) : BlogDetailIntent
}

sealed interface BlogDetailEffect {
    data class ShowError(val message: Any) : BlogDetailEffect
}
