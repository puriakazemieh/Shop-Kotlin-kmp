package com.kazemieh.domain.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.repository.AdminRepository

class UpdateAdminOrderStatusUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(id: Long, status: String): AppResult<Unit> {
        return repository.updateOrderStatus(id, status)
    }
}
