package com.kazemieh.domain.usecase

import com.kazemieh.domain.AuthRepository
import com.kazemieh.domain.model.User

class RegisterUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return repository.register(email, password)
    }
}