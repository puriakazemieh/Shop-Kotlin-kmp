package com.kazemieh.domain.blog.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.blog.BlogCategory
import com.kazemieh.domain.blog.BlogRepository

class CreateBlogCategoryUseCase(private val repository: BlogRepository) {
    suspend operator fun invoke(category: BlogCategory) = repository.createCategory(category)
}

class UpdateBlogCategoryUseCase(private val repository: BlogRepository) {
    suspend operator fun invoke(id: Long, category: BlogCategory) = repository.updateCategory(id, category)
}

class DeleteBlogCategoryUseCase(private val repository: BlogRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteCategory(id)
}
