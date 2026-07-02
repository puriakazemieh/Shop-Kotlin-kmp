package com.kazemieh.data.catalog.source

import com.kazemieh.network.catalog.CatalogApi
import com.kazemieh.network.catalog.dto.response.*
import com.kazemieh.domain.catalog.*
import com.kazemieh.domain.admin.AdminPage
import com.kazemieh.network.common.*
import com.kazemieh.common.*




class CatalogDataSourceImpl(
    private val api: CatalogApi
) : CatalogDataSource {

    override suspend fun getCategories(): AppResult<List<CategoryResponse>> = safeApiCall {
        api.getCategories()
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
        sort: String?,
        discountedOnly: Boolean
    ): AppResult<PageResponse<ProductSummaryResponse>> = safeApiCall {
        api.getProducts(query, categoryId, options, minPrice, maxPrice, inStock, page, size, sort, discountedOnly)
    }

    override suspend fun getProductDetail(slug: String): AppResult<ProductDetailResponse> = safeApiCall {
        api.getProductDetail(slug)
    }

    override suspend fun getActiveCampaign(): AppResult<CampaignResponse?> = safeApiCall {
        api.getActiveCampaign()
    }

    override suspend fun getBanners(): AppResult<List<BannerResponse>> = safeApiCall {
        api.getBanners()
    }

    override suspend fun requestBackInStock(productId: Long, variantId: Long): AppResult<Unit> = safeApiCall {
        api.requestBackInStock(productId, variantId)
    }
}
