package com.kazemieh.data.recentlyviewed.source

import com.kazemieh.common.AppResult
import com.kazemieh.network.catalog.dto.response.ProductSummaryResponse
import com.kazemieh.network.common.PageResponse

interface RecentlyViewedDataSource {
    suspend fun record(productId: Long): AppResult<Unit>
    suspend fun getRecentlyViewed(page: Int, size: Int): AppResult<PageResponse<ProductSummaryResponse>>
}
