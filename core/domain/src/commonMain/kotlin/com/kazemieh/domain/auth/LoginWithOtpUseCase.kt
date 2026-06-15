package com.kazemieh.domain.auth

import com.kazemieh.common.AppResult

class LoginWithOtpUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(mobile: String, otpCode: String): AppResult<Unit> {
        return repository.loginWithOtp(mobile, otpCode)
    }
}
