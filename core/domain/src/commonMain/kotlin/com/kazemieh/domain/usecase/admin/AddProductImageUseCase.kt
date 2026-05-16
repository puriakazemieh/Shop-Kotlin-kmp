package com.kazemieh.domain.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.admin.AdminProductImage
import com.kazemieh.domain.repository.AdminRepository

class AddProductImageUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(productId: Long, url: String, sortOrder: Int?): AppResult<AdminProductImage> {
        return repository.addImage(productId, url, sortOrder)
    }
}
