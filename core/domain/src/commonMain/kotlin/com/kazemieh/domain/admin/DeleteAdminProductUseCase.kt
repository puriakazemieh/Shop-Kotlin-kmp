package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.AdminRepository

class DeleteAdminProductUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(id: Long): AppResult<Unit> {
        return repository.deleteProduct(id)
    }
}
