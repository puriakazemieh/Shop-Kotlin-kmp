package com.kazemieh.domain.usecase

import com.kazemieh.common.AppResult
import com.kazemieh.domain.repository.AuthRepository
import com.kazemieh.domain.model.User

class RegisterUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AppResult<Unit> {
        return repository.register(email, password)
    }
}