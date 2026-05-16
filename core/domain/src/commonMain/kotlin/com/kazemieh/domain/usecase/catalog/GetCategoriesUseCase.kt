package com.kazemieh.domain.usecase.catalog

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.Category
import com.kazemieh.domain.repository.CatalogRepository

class GetCategoriesUseCase(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(): AppResult<List<Category>> {
        return repository.getCategories()
    }
}
