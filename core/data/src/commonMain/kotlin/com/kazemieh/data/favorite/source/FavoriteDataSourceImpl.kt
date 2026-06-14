package com.kazemieh.data.favorite.source

import com.kazemieh.common.AppResult
import com.kazemieh.network.catalog.dto.response.ProductSummaryResponse
import com.kazemieh.network.common.PageResponse
import com.kazemieh.network.common.safeApiCall
import com.kazemieh.network.favorite.FavoriteApi

class FavoriteDataSourceImpl(private val api: FavoriteApi) : FavoriteDataSource {
    override suspend fun addToFavorites(productId: Long): AppResult<Unit> = safeApiCall {
        api.addToFavorites(productId)
    }

    override suspend fun removeFromFavorites(productId: Long): AppResult<Unit> = safeApiCall {
        api.removeFromFavorites(productId)
    }

    override suspend fun getFavorites(page: Int, size: Int): AppResult<PageResponse<ProductSummaryResponse>> = safeApiCall {
        api.getFavorites(page, size)
    }
}
