package com.kazemieh.data.favorite.repository

import com.kazemieh.common.AppResult
import com.kazemieh.common.map
import com.kazemieh.data.admin.mapper.toAdminPage
import com.kazemieh.data.catalog.mapper.toCatalogDomain
import com.kazemieh.data.favorite.source.FavoriteDataSource
import com.kazemieh.domain.admin.AdminPage
import com.kazemieh.domain.catalog.ProductSummary
import com.kazemieh.domain.favorite.FavoriteRepository

class FavoriteRepositoryImpl(private val dataSource: FavoriteDataSource) : FavoriteRepository {
    override suspend fun addToFavorites(productId: Long): AppResult<Unit> {
        return dataSource.addToFavorites(productId)
    }

    override suspend fun removeFromFavorites(productId: Long): AppResult<Unit> {
        return dataSource.removeFromFavorites(productId)
    }

    override suspend fun getFavorites(page: Int, size: Int): AppResult<AdminPage<ProductSummary>> {
        return dataSource.getFavorites(page, size).map { response ->
            response.toAdminPage { it.toCatalogDomain() }
        }
    }
}
