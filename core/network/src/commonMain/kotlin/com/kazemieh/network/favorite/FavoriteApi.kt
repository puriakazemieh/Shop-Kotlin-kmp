package com.kazemieh.network.favorite

import com.kazemieh.network.catalog.dto.response.ProductSummaryResponse
import com.kazemieh.network.common.PageResponse

interface FavoriteApi {
    suspend fun addToFavorites(productId: Long)
    suspend fun removeFromFavorites(productId: Long)
    suspend fun getFavorites(page: Int = 0, size: Int = 20): PageResponse<ProductSummaryResponse>
}
