package com.kazemieh.domain.usecase

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.Profile
import com.kazemieh.domain.repository.ProfileRepository

class GetProfileUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(): AppResult<Profile> {
        return repository.getProfile()
    }
}
