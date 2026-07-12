package com.kazemieh.domain.catalog

import com.kazemieh.common.AppResult

class RequestBackInStockUseCase(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(productId: Long, variantId: Long): AppResult<Unit> {
        return repository.requestBackInStock(productId, variantId)
    }
}
