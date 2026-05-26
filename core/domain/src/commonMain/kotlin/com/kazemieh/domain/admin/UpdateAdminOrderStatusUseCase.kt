package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.AdminRepository

class UpdateAdminOrderStatusUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(id: Long, status: String): AppResult<Unit> {
        return repository.updateOrderStatus(id, status)
    }
}
