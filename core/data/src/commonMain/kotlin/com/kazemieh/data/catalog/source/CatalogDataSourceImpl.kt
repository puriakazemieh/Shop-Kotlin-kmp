package com.kazemieh.data.catalog.source

import com.kazemieh.common.AppResult
import com.kazemieh.network.CatalogApi
import com.kazemieh.network.dto.PageResponse
import com.kazemieh.network.dto.catalog.response.*
import com.kazemieh.network.safeApiCall

class CatalogDataSourceImpl(
    private val api: CatalogApi
) : CatalogDataSource {

    override suspend fun getCategories(): AppResult<List<CategoryResponse>> = safeApiCall {
        api.getCategories()
    }

    override suspend fun getSizes(): AppResult<List<SizeResponse>> = safeApiCall {
        api.getSizes()
    }

    override suspend fun getColors(): AppResult<List<ColorResponse>> = safeApiCall {
        api.getColors()
    }

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
    ): AppResult<PageResponse<ProductSummaryResponse>> = safeApiCall {
        api.getProducts(query, categoryId, sizeId, colorId, minPrice, maxPrice, inStock, page, size, sort)
    }

    override suspend fun getProductDetail(slug: String): AppResult<ProductDetailResponse> = safeApiCall {
        api.getProductDetail(slug)
    }
}
