package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.AdminOrderDetail
import com.kazemieh.domain.admin.AdminRepository

class GetAdminOrderDetailUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(id: Long): AppResult<AdminOrderDetail> {
        return repository.getOrderDetail(id)
    }
}
