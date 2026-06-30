package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.AdminCreateVariant
import com.kazemieh.domain.admin.AdminProduct
import com.kazemieh.domain.admin.AdminRepository

class CreateAdminProductUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(
        categoryId: Long?,
        title: String,
        slug: String,
        description: String?,
        brand: String? = null,
        attributes: List<com.kazemieh.domain.catalog.ProductAttribute>? = null,
        basePrice: Double?,
        discountedPrice: Double? = null,
        sku: String? = null,
        initialOnHand: Int? = null,
        isActive: Boolean,
        variants: List<AdminCreateVariant>? = null
    ): AppResult<AdminProduct> {
        return repository.createProduct(categoryId, title, slug, description, brand, attributes, basePrice, discountedPrice, sku, initialOnHand, isActive, variants)
    }
}
