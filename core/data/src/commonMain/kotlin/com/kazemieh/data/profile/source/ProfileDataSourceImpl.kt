package com.kazemieh.data.profile.source

import com.kazemieh.common.AppResult
import com.kazemieh.data.profile.mapper.toDomain
import com.kazemieh.data.profile.mapper.toUpdateRequest
import com.kazemieh.domain.model.Profile
import com.kazemieh.network.ProfileApi
import com.kazemieh.network.safeApiCall

class ProfileDataSourceImpl(
    private val profileApi: ProfileApi
) : ProfileDataSource {

    override suspend fun getProfile(): AppResult<Profile> = safeApiCall {
        profileApi.getProfile().toDomain()
    }

    override suspend fun updateProfile(request: Profile): AppResult<Profile> = safeApiCall {
        profileApi.updateProfile(request = request.toUpdateRequest()).toDomain()
    }
}