package com.kazemieh.domain.profile

import com.kazemieh.common.AppResult
import com.kazemieh.domain.profile.Profile
import com.kazemieh.domain.profile.ProfileRepository

class UpdateProfileUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(profile: Profile): AppResult<Profile> {
        return repository.updateProfile(profile)
    }
}
