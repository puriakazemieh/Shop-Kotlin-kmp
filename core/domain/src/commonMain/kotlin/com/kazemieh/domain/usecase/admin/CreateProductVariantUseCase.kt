package com.kazemieh.domain.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.admin.AdminVariant
import com.kazemieh.domain.repository.AdminRepository

class CreateProductVariantUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(
        productId: Long,
        optionType: String,
        optionValue: String,
        sku: String,
        price: Double,
        compareAtPrice: Double?,
        isActive: Boolean,
        initialOnHand: Int
    ): AppResult<AdminVariant> {
        return repository.createVariant(productId, optionType, optionValue, sku, price, compareAtPrice, isActive, initialOnHand)
    }
}
