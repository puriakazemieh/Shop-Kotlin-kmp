package com.kazemieh.domain.blog.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.blog.Blog
import com.kazemieh.domain.blog.BlogRepository

class CreateBlogUseCase(
    private val repository: BlogRepository
) {
    suspend operator fun invoke(blog: Blog): AppResult<Blog> {
        return repository.createBlog(blog)
    }
}
