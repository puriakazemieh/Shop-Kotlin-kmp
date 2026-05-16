package com.kazemieh.domain.usecase.admin

import com.kazemieh.common.AppResult
import com.kazemieh.domain.repository.AdminRepository
import com.kazemieh.domain.repository.Color

class CreateColorUseCase(
    private val repository: AdminRepository
) {
    suspend operator fun invoke(name: String, hex: String?): AppResult<Color> {
        return repository.createColor(name, hex)
    }
}
