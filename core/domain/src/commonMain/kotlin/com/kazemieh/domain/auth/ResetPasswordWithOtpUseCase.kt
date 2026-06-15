package com.kazemieh.domain.auth

import com.kazemieh.common.AppResult

class ResetPasswordWithOtpUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(mobile: String, otpCode: String, newPassword: String): AppResult<Unit> {
        return repository.resetPasswordWithOtp(mobile, otpCode, newPassword)
    }
}
