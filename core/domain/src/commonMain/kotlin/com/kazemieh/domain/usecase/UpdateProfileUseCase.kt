package com.kazemieh.domain.usecase

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.Profile
import com.kazemieh.domain.repository.ProfileRepository

class UpdateProfileUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(profile: Profile): AppResult<Profile> {
        return repository.updateProfile(profile)
    }
}
