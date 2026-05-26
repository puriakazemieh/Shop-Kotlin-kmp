package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.AdminVariant
import com.kazemieh.domain.admin.AdminVariantOption
import com.kazemieh.domain.admin.AdminRepository

class UpdateProductVariantUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(
        variantId: Long,
        sku: String?,
        price: Double?,
        compareAtPrice: Double?,
        options: List<AdminVariantOption>?,
        isActive: Boolean?
    ): AppResult<AdminVariant> {
        return repository.updateVariant(variantId, sku, price, compareAtPrice, options, isActive)
    }
}
