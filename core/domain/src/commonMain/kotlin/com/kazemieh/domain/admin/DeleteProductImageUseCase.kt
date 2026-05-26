package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.AdminRepository

class DeleteProductImageUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(productId: Long, imageId: Long): AppResult<Unit> {
        return repository.deleteImage(productId, imageId)
    }
}
