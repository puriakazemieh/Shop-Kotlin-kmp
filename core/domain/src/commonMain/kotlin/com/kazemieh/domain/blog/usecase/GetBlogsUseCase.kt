package com.kazemieh.domain.blog.usecase

import com.kazemieh.common.AppResult
import com.kazemieh.domain.blog.BlogList
import com.kazemieh.domain.blog.BlogRepository

class GetBlogsUseCase(
    private val repository: BlogRepository
) {
    suspend operator fun invoke(
        page: Int = 0,
        size: Int = 20,
        categoryId: Long? = null,
        searchQuery: String? = null
    ): AppResult<BlogList> {
        return repository.getBlogs(page, size, categoryId, searchQuery)
    }
}
