package com.kazemieh.domain.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.admin.AdminVariant
import com.kazemieh.domain.repository.AdminRepository

class UpdateProductVariantUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(
        variantId: Long,
        sku: String?,
        price: Double?,
        compareAtPrice: Double?,
        isActive: Boolean?
    ): AppResult<AdminVariant> {
        return repository.updateVariant(variantId, sku, price, compareAtPrice, isActive)
    }
}
