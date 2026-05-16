package com.kazemieh.domain.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.admin.AdminProduct
import com.kazemieh.domain.repository.AdminPage
import com.kazemieh.domain.repository.AdminRepository

class GetAdminProductsUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(page: Int, size: Int, includeInactive: Boolean, query: String? = null): AppResult<AdminPage<AdminProduct>> {
        return repository.listProducts(page, size, includeInactive, query)
    }
}
