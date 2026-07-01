package com.kazemieh.data.recentlyviewed.source

import com.kazemieh.common.AppResult
import com.kazemieh.network.catalog.dto.response.ProductSummaryResponse
import com.kazemieh.network.common.PageResponse
import com.kazemieh.network.common.safeApiCall
import com.kazemieh.network.recentlyviewed.RecentlyViewedApi

class RecentlyViewedDataSourceImpl(private val api: RecentlyViewedApi) : RecentlyViewedDataSource {
    override suspend fun record(productId: Long): AppResult<Unit> = safeApiCall {
        api.record(productId)
    }

    override suspend fun getRecentlyViewed(page: Int, size: Int): AppResult<PageResponse<ProductSummaryResponse>> = safeApiCall {
        api.getRecentlyViewed(page, size)
    }
}
