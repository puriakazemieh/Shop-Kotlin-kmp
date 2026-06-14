package com.kazemieh.network.favorite

import com.kazemieh.network.catalog.dto.response.ProductSummaryResponse
import com.kazemieh.network.common.PageResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post

class FavoriteApiImpl(private val client: HttpClient) : FavoriteApi {
    override suspend fun addToFavorites(productId: Long) {
        client.post("/api/favorites/$productId")
    }

    override suspend fun removeFromFavorites(productId: Long) {
        client.delete("/api/favorites/$productId")
    }

    override suspend fun getFavorites(page: Int, size: Int): PageResponse<ProductSummaryResponse> {
        return client.get("/api/favorites") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }
}
