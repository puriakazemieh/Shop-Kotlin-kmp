package com.kazemieh.domain.catalog

import com.kazemieh.common.AppResult

class SubscribeToPriceAlertUseCase(
    private val repository: CatalogRepository
) {
    suspend operator fun invoke(productId: Long, variantId: Long, targetPrice: Double): AppResult<Unit> {
        return repository.subscribeToPriceAlert(productId, variantId, targetPrice)
    }
}
