package com.kazemieh.domain.blog.usecase

import com.kazemieh.common.AppResult
import com.kazemieh.domain.blog.Blog
import com.kazemieh.domain.blog.BlogRepository

class GetFeaturedBlogsUseCase(
    private val repository: BlogRepository
) {
    suspend operator fun invoke(): AppResult<List<Blog>> {
        return repository.getFeaturedBlogs()
    }
}
