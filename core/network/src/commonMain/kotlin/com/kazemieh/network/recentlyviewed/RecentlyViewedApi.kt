package com.kazemieh.network.recentlyviewed

import com.kazemieh.network.catalog.dto.response.ProductSummaryResponse
import com.kazemieh.network.common.PageResponse

interface RecentlyViewedApi {
    suspend fun record(productId: Long)
    suspend fun getRecentlyViewed(page: Int = 0, size: Int = 20): PageResponse<ProductSummaryResponse>
}
