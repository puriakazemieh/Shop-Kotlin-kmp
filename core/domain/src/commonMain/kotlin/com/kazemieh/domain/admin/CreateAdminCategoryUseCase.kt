package com.kazemieh.domain.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.AdminCategory
import com.kazemieh.domain.admin.AdminRepository

class CreateAdminCategoryUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(name: String, slug: String, parentId: Long?): AppResult<AdminCategory> {
        return repository.createCategory(name, slug, parentId)
    }
}
