package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.AdminProduct
import com.kazemieh.domain.admin.AdminRepository

class UpdateAdminProductUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(
        id: Long,
        categoryId: Long?,
        title: String?,
        slug: String?,
        description: String?,
        basePrice: Double?,
        discountedPrice: Double? = null,
        isActive: Boolean?
    ): AppResult<AdminProduct> {
        return repository.updateProduct(id, categoryId, title, slug, description, basePrice, discountedPrice, isActive)
    }
}
