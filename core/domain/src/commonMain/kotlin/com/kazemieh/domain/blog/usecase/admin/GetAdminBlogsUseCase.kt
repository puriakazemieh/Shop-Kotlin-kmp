package com.kazemieh.domain.blog.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.blog.BlogList
import com.kazemieh.domain.blog.BlogRepository

class GetAdminBlogsUseCase(
    private val repository: BlogRepository
) {
    suspend operator fun invoke(page: Int = 0, size: Int = 20): AppResult<BlogList> {
        return repository.getAdminBlogs(page, size)
    }
}
