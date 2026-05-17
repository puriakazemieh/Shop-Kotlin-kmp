package com.kazemieh.domain.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.repository.AdminRepository
import com.kazemieh.domain.repository.Color

class GetAdminColorsUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(): AppResult<List<Color>> {
        return repository.listColors()
    }
}
