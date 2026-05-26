package com.kazemieh.domain.catalog

import com.kazemieh.common.AppResult
import com.kazemieh.domain.catalog.Category
import com.kazemieh.domain.catalog.CatalogRepository

class GetCategoriesUseCase(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(): AppResult<List<Category>> {
        return repository.getCategories()
    }
}
