package com.kazemieh.domain.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.repository.AdminCategory
import com.kazemieh.domain.repository.AdminRepository

class CreateAdminCategoryUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(name: String, slug: String, parentId: Long?): AppResult<AdminCategory> {
        return repository.createCategory(name, slug, parentId)
    }
}
