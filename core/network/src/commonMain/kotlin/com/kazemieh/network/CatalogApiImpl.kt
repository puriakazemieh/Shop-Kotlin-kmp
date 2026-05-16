package com.kazemieh.network

import com.kazemieh.network.dto.PageResponse
import com.kazemieh.network.dto.catalog.response.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*

class CatalogApiImpl(
    private val client: HttpClient
) : CatalogApi {

    override suspend fun getCategories(): List<CategoryResponse> =
        client.get("api/categories").body()

    override suspend fun getSizes(): List<SizeResponse> =
        client.get("api/sizes").body()

    override suspend fun getColors(): List<ColorResponse> =
        client.get("api/colors").body()

    override suspend fun getProducts(
        query: String?,
        categoryId: Long?,
        sizeId: Long?,
        colorId: Long?,
        minPrice: Double?,
        maxPrice: Double?,
        inStock: Boolean?,
        page: Int,
        size: Int,
        sort: String?
    ): PageResponse<ProductSummaryResponse> =
        client.get("api/products") {
            parameter("q", query)
            parameter("categoryId", categoryId)
            parameter("sizeId", sizeId)
            parameter("colorId", colorId)
            parameter("minPrice", minPrice)
            parameter("maxPrice", maxPrice)
            parameter("inStock", inStock)
            parameter("page", page)
            parameter("size", size)
            parameter("sort", sort)
        }.body()

    override suspend fun getProductDetail(slug: String): ProductDetailResponse =
        client.get("api/products/$slug").body()
}
