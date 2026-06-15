package com.kazemieh.domain.auth

import com.kazemieh.common.AppResult
import com.kazemieh.domain.auth.AuthRepository

class ForgotPasswordUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String? = null, mobile: String? = null): AppResult<Unit> {
        return repository.forgotPassword(email, mobile)
    }
}
