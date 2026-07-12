package com.kazemieh.network.catalog

import com.kazemieh.network.catalog.dto.response.*

import com.kazemieh.network.common.PageResponse
import com.kazemieh.network.catalog.dto.*

interface CatalogApi {
    suspend fun getCategories(): List<CategoryResponse>
    suspend fun getProducts(
        query: String? = null,
        categoryId: Long? = null,
        options: Map<String, String>? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        inStock: Boolean? = null,
        page: Int = 0,
        size: Int = 20,
        sort: String? = null,
        discountedOnly: Boolean = false
    ): PageResponse<ProductSummaryResponse>
    suspend fun getProductDetail(slug: String): ProductDetailResponse
    suspend fun getActiveCampaign(): CampaignResponse?
    suspend fun getBanners(): List<BannerResponse>
    suspend fun requestBackInStock(productId: Long, variantId: Long)
    suspend fun getFrequentlyBoughtTogether(productId: Long): List<ProductSummaryResponse>
    suspend fun subscribeToPriceAlert(productId: Long, variantId: Long, targetPrice: Double)
}
