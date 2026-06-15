package com.kazemieh.domain.auth

import com.kazemieh.common.AppResult
import com.kazemieh.domain.auth.AuthRepository
import com.kazemieh.domain.profile.User

class RegisterUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String? = null, mobile: String? = null, password: String): AppResult<Unit> {
        return repository.register(email, mobile, password)
    }
}
