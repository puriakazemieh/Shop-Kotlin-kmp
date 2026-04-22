package com.kazemieh.domain.usecase

import com.kazemieh.domain.AuthRepository
import com.kazemieh.domain.common.AppResult
import com.kazemieh.domain.model.User

class RegisterUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AppResult<User> {
        return repository.register(email, password)
    }
}