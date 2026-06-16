package com.kazemieh.domain.blog.usecase

import com.kazemieh.common.AppResult
import com.kazemieh.domain.blog.Blog
import com.kazemieh.domain.blog.BlogRepository

class GetBlogDetailUseCase(
    private val repository: BlogRepository
) {
    suspend operator fun invoke(slug: String): AppResult<Blog> {
        return repository.getBlogDetail(slug)
    }
}
