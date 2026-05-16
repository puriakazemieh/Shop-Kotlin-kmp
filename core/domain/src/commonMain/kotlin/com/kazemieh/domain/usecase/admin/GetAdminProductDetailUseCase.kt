package com.kazemieh.domain.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.admin.AdminProductDetail
import com.kazemieh.domain.repository.AdminRepository

class GetAdminProductDetailUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(id: Long): AppResult<AdminProductDetail> {
        return repository.getProductDetail(id)
    }
}
