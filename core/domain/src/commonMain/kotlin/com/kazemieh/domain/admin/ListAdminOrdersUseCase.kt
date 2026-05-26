package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.AdminOrderSummary
import com.kazemieh.domain.admin.AdminPage
import com.kazemieh.domain.admin.AdminRepository

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
