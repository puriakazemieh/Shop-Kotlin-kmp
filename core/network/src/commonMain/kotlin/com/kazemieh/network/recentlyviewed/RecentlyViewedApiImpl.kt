package com.kazemieh.network.recentlyviewed

import com.kazemieh.network.catalog.dto.response.ProductSummaryResponse
import com.kazemieh.network.common.PageResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post

class RecentlyViewedApiImpl(private val client: HttpClient) : RecentlyViewedApi {
    override suspend fun record(productId: Long) {
        client.post("/api/recently-viewed/$productId")
    }

    override suspend fun getRecentlyViewed(page: Int, size: Int): PageResponse<ProductSummaryResponse> {
        return client.get("/api/recently-viewed") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }
}
