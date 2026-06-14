package com.kazemieh.data.favorite.source

import com.kazemieh.common.AppResult
import com.kazemieh.network.catalog.dto.response.ProductSummaryResponse
import com.kazemieh.network.common.PageResponse

interface FavoriteDataSource {
    suspend fun addToFavorites(productId: Long): AppResult<Unit>
    suspend fun removeFromFavorites(productId: Long): AppResult<Unit>
    suspend fun getFavorites(page: Int, size: Int): AppResult<PageResponse<ProductSummaryResponse>>
}
