package com.kazemieh.domain.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.repository.AdminRepository
import com.kazemieh.domain.repository.Size

class UpdateSizeUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(id: Long, name: String?, sortOrder: Int?): AppResult<Size> {
        return repository.updateSize(id, name, sortOrder)
    }
}
