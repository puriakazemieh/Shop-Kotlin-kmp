package com.kazemieh.domain.catalog

import com.kazemieh.common.AppResult
import com.kazemieh.domain.catalog.ProductDetail
import com.kazemieh.domain.catalog.CatalogRepository

class GetProductDetailUseCase(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(slug: String): AppResult<ProductDetail> {
        return repository.getProductDetail(slug)
    }
}
