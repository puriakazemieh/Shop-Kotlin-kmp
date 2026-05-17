package com.kazemieh.domain.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.repository.AdminRepository

class DeleteProductImageUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(productId: Long, imageId: Long): AppResult<Unit> {
        return repository.deleteImage(productId, imageId)
    }
}
