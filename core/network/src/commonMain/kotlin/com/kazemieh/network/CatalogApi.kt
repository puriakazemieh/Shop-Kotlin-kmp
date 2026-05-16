package com.kazemieh.network

import com.kazemieh.network.dto.PageResponse
import com.kazemieh.network.dto.catalog.response.*

interface CatalogApi {
    suspend fun getCategories(): List<CategoryResponse>
    suspend fun getSizes(): List<SizeResponse>
    suspend fun getColors(): List<ColorResponse>
    suspend fun getProducts(
        query: String? = null,
        categoryId: Long? = null,
        sizeId: Long? = null,
        colorId: Long? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        inStock: Boolean? = null,
        page: Int = 0,
        size: Int = 20,
        sort: String? = null
    ): PageResponse<ProductSummaryResponse>
    suspend fun getProductDetail(slug: String): ProductDetailResponse
}
