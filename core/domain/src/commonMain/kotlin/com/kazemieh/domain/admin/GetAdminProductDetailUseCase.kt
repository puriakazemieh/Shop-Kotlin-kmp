package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.AdminProductDetail
import com.kazemieh.domain.admin.AdminRepository

class GetAdminProductDetailUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(id: Long): AppResult<AdminProductDetail> {
        return repository.getProductDetail(id)
    }
}
