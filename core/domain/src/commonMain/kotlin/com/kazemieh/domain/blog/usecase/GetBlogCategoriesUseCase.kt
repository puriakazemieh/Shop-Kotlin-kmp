package com.kazemieh.domain.blog.usecase

import com.kazemieh.common.AppResult
import com.kazemieh.domain.blog.BlogCategory
import com.kazemieh.domain.blog.BlogRepository

class GetBlogCategoriesUseCase(
    private val repository: BlogRepository
) {
    suspend operator fun invoke(): AppResult<List<BlogCategory>> {
        return repository.getCategories()
    }
}
