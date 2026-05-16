package com.kazemieh.domain.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.repository.AdminRepository
import com.kazemieh.domain.repository.Size

class CreateSizeUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(name: String, sortOrder: Int): AppResult<Size> {
        return repository.createSize(name, sortOrder)
    }
}
