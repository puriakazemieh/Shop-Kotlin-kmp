package com.kazemieh.data.catalog.source

import com.kazemieh.common.AppResult
import com.kazemieh.network.dto.PageResponse
import com.kazemieh.network.dto.catalog.response.*

interface CatalogDataSource {
    suspend fun getCategories(): AppResult<List<CategoryResponse>>
    suspend fun getProducts(
        query: String?,
        categoryId: Long?,
        options: Map<String, String>?,
        minPrice: Double?,
        maxPrice: Double?,
        inStock: Boolean?,
        page: Int,
        size: Int,
        sort: String?
    ): AppResult<PageResponse<ProductSummaryResponse>>
    suspend fun getProductDetail(slug: String): AppResult<ProductDetailResponse>
}
