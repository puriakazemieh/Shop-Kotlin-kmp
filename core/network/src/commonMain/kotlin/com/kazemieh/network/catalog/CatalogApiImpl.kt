package com.kazemieh.network.catalog

import com.kazemieh.network.catalog.dto.response.*

import com.kazemieh.network.common.safeApiCallRaw

import com.kazemieh.network.common.PageResponse
import com.kazemieh.network.catalog.dto.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.HttpStatusCode

class CatalogApiImpl(
    private val client: HttpClient
) : CatalogApi {

    override suspend fun getCategories(): List<CategoryResponse> = safeApiCallRaw {
        client.get("api/categories")
    }

    override suspend fun getProducts(
        query: String?,
        categoryId: Long?,
        options: Map<String, String>?,
        minPrice: Double?,
        maxPrice: Double?,
        inStock: Boolean?,
        page: Int,
        size: Int,
        sort: String?
    ): PageResponse<ProductSummaryResponse> = safeApiCallRaw {
        client.get("api/products") {
            parameter("q", query)
            parameter("categoryId", categoryId)
            options?.forEach { (key, value) ->
                parameter("options.$key", value)
            }
            parameter("minPrice", minPrice)
            parameter("maxPrice", maxPrice)
            parameter("inStock", inStock)
            parameter("page", page)
            parameter("size", size)
            parameter("sort", sort)
        }
    }

    override suspend fun getProductDetail(slug: String): ProductDetailResponse = safeApiCallRaw {
        client.get("api/products/$slug")
    }

    override suspend fun getActiveCampaign(): CampaignResponse? {
        val response = client.get("api/campaigns/active")
        return if (response.status == HttpStatusCode.NoContent) null
        else response.body<CampaignResponse>()
    }

    override suspend fun getBanners(): List<BannerResponse> = safeApiCallRaw {
        client.get("api/banners")
    }
}
