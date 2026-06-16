package com.kazemieh.domain.blog.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.blog.BlogRepository

class DeleteBlogUseCase(
    private val repository: BlogRepository
) {
    suspend operator fun invoke(id: Long): AppResult<Unit> {
        return repository.deleteBlog(id)
    }
}
