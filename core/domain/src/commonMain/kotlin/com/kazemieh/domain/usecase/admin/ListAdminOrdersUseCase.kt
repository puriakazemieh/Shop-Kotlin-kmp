package com.kazemieh.domain.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.admin.AdminOrderSummary
import com.kazemieh.domain.repository.AdminPage
import com.kazemieh.domain.repository.AdminRepository

class ListAdminOrdersUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(
        status: String? = null,
        userId: Long? = null,
        page: Int = 0,
        size: Int = 20
    ): AppResult<AdminPage<AdminOrderSummary>> {
        return repository.listOrders(status, userId, page, size)
    }
}
