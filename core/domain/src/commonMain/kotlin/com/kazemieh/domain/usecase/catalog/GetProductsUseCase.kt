package com.kazemieh.domain.usecase.catalog

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.ProductSummary
import com.kazemieh.domain.repository.AdminPage
import com.kazemieh.domain.repository.CatalogRepository

class GetProductsUseCase(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(
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
    ): AppResult<AdminPage<ProductSummary>> {
        return repository.getProducts(
            query, categoryId, sizeId, colorId, minPrice, maxPrice, inStock, page, size, sort
        )
    }
}
