package com.kazemieh.data.favorite.repository

import com.kazemieh.common.AppResult
import com.kazemieh.common.map
import com.kazemieh.data.admin.mapper.toAdminPage
import com.kazemieh.data.catalog.mapper.toCatalogDomain
import com.kazemieh.data.favorite.source.FavoriteDataSource
import com.kazemieh.domain.admin.AdminPage
import com.kazemieh.domain.catalog.ProductSummary
import com.kazemieh.domain.favorite.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FavoriteRepositoryImpl(private val dataSource: FavoriteDataSource) : FavoriteRepository {
    private val favoriteIds = MutableStateFlow<Set<Long>>(emptySet())

    override fun observeFavoriteIds(): Flow<Set<Long>> = favoriteIds

    override suspend fun refreshFavorites(): AppResult<Unit> {
        return getFavorites(0, 100).map { Unit }
    }

    override suspend fun updateFavoriteStatus(productId: Long, isFavorite: Boolean) {
        favoriteIds.update { 
            if (isFavorite) it + productId else it - productId 
        }
    }

    override suspend fun addToFavorites(productId: Long): AppResult<Unit> {
        val result = dataSource.addToFavorites(productId)
        if (result is AppResult.Success) {
            favoriteIds.update { it + productId }
        }
        return result
    }

    override suspend fun removeFromFavorites(productId: Long): AppResult<Unit> {
        val result = dataSource.removeFromFavorites(productId)
        if (result is AppResult.Success) {
            favoriteIds.update { it - productId }
        }
        return result
    }

    override suspend fun getFavorites(page: Int, size: Int): AppResult<AdminPage<ProductSummary>> {
        return dataSource.getFavorites(page, size).map { response ->
            val adminPage = response.toAdminPage { 
                it.toCatalogDomain().copy(isFavorite = true) 
            }
            val ids = adminPage.items.map { it.id }.toSet()
            if (page == 0) {
                favoriteIds.update { ids }
            } else {
                favoriteIds.update { it + ids }
            }
            adminPage
        }
    }
}
