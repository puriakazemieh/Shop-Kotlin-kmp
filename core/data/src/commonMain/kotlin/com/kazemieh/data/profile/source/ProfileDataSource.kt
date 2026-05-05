package com.kazemieh.data.profile.source

import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.Profile

interface ProfileDataSource {
    suspend fun getProfile(): AppResult<Profile>
    suspend fun updateProfile(request: Profile): AppResult<Profile>
}