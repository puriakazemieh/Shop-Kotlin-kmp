package com.kazemieh.domain.usecase.catalog

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.ProductDetail
import com.kazemieh.domain.repository.CatalogRepository

class GetProductDetailUseCase(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(slug: String): AppResult<ProductDetail> {
        return repository.getProductDetail(slug)
    }
}
