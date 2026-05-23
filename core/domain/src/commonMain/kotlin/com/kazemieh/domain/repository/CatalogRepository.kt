package com.kazemieh.domain.repository

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.Category
import com.kazemieh.domain.model.ProductDetail
import com.kazemieh.domain.model.ProductSummary

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
        sort: String? = null
    ): AppResult<AdminPage<ProductSummary>>
    suspend fun getProductDetail(slug: String): AppResult<ProductDetail>
}
