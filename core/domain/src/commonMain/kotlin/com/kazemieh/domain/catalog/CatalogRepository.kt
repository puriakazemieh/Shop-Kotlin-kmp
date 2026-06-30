package com.kazemieh.domain.catalog

import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.AdminPage
import com.kazemieh.domain.catalog.Category
import com.kazemieh.domain.catalog.ProductDetail
import com.kazemieh.domain.catalog.ProductSummary

interface CatalogRepository {
    suspend fun getCategories(): AppResult<List<Category>>
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
    ): AppResult<AdminPage<ProductSummary>>
    suspend fun getProductDetail(slug: String): AppResult<ProductDetail>
    suspend fun getActiveCampaign(): AppResult<Campaign?>
    suspend fun getBanners(): AppResult<List<Banner>>
}
