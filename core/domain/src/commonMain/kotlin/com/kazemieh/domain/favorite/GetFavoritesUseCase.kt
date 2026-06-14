package com.kazemieh.domain.favorite

import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.AdminPage
import com.kazemieh.domain.catalog.ProductSummary

class GetFavoritesUseCase(private val repository: FavoriteRepository) {
    suspend operator fun invoke(page: Int = 0, size: Int = 20): AppResult<AdminPage<ProductSummary>> {
        return repository.getFavorites(page, size)
    }
}
