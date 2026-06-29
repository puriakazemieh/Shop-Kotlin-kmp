package com.kazemieh.data.catalog.source

import com.kazemieh.network.catalog.dto.response.*
import com.kazemieh.domain.catalog.*
import com.kazemieh.domain.admin.AdminPage
import com.kazemieh.network.common.*
import com.kazemieh.common.*




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
    suspend fun getActiveCampaign(): AppResult<CampaignResponse?>
    suspend fun getBanners(): AppResult<List<BannerResponse>>
}
