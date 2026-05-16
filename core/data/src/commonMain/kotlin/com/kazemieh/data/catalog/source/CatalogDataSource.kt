package com.kazemieh.data.catalog.source

import com.kazemieh.common.AppResult
import com.kazemieh.network.dto.PageResponse
import com.kazemieh.network.dto.catalog.response.*

interface CatalogDataSource {
    suspend fun getCategories(): AppResult<List<CategoryResponse>>
    suspend fun getSizes(): AppResult<List<SizeResponse>>
    suspend fun getColors(): AppResult<List<ColorResponse>>
    suspend fun getProducts(
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
    ): AppResult<PageResponse<ProductSummaryResponse>>
    suspend fun getProductDetail(slug: String): AppResult<ProductDetailResponse>
}
