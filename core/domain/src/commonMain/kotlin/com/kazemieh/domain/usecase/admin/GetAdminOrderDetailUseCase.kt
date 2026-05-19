package com.kazemieh.domain.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.admin.AdminOrderDetail
import com.kazemieh.domain.repository.AdminRepository

class GetAdminOrderDetailUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(id: Long): AppResult<AdminOrderDetail> {
        return repository.getOrderDetail(id)
    }
}
