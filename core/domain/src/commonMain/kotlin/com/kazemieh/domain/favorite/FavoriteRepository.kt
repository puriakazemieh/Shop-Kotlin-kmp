package com.kazemieh.domain.favorite

import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.AdminPage
import com.kazemieh.domain.catalog.ProductSummary

interface FavoriteRepository {
    suspend fun addToFavorites(productId: Long): AppResult<Unit>
    suspend fun removeFromFavorites(productId: Long): AppResult<Unit>
    suspend fun getFavorites(page: Int = 0, size: Int = 20): AppResult<AdminPage<ProductSummary>>
}
