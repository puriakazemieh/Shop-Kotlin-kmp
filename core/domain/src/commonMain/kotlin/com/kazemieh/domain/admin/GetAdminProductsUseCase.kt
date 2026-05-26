package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.AdminProduct
import com.kazemieh.domain.admin.AdminPage
import com.kazemieh.domain.admin.AdminRepository

class GetAdminProductsUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(page: Int, size: Int, includeInactive: Boolean, query: String? = null): AppResult<AdminPage<AdminProduct>> {
        return repository.listProducts(page, size, includeInactive, query)
    }
}
