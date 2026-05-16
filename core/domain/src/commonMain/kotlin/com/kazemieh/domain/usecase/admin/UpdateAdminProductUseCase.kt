package com.kazemieh.domain.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.admin.AdminProduct
import com.kazemieh.domain.repository.AdminRepository

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
        isActive: Boolean?
    ): AppResult<AdminProduct> {
        return repository.updateProduct(id, categoryId, title, slug, description, basePrice, isActive)
    }
}
