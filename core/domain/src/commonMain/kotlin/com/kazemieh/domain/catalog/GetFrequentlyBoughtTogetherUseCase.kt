package com.kazemieh.domain.catalog

import com.kazemieh.common.AppResult

class GetFrequentlyBoughtTogetherUseCase(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(productId: Long): AppResult<List<ProductSummary>> {
        return repository.getFrequentlyBoughtTogether(productId)
    }
}
