package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.AdminVariant
import com.kazemieh.domain.admin.AdminVariantOption
import com.kazemieh.domain.admin.AdminRepository

class CreateProductVariantUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(
        productId: Long,
        options: List<AdminVariantOption>,
        sku: String,
        price: Double,
        compareAtPrice: Double?,
        isActive: Boolean,
        initialOnHand: Int
    ): AppResult<AdminVariant> {
        return repository.createVariant(productId, options, sku, price, compareAtPrice, isActive, initialOnHand)
    }
}
