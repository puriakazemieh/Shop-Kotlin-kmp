package com.kazemieh.domain.auth

import com.kazemieh.common.AppResult

class SendLoginOtpUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(mobile: String): AppResult<Unit> {
        return repository.sendLoginOtp(mobile)
    }
}
