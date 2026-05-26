package com.kazemieh.data.profile.source

import com.kazemieh.network.profile.ProfileApi
import com.kazemieh.data.profile.mapper.*
import com.kazemieh.network.profile.dto.request.*
import com.kazemieh.network.profile.dto.response.*
import com.kazemieh.network.address.dto.request.*
import com.kazemieh.network.address.dto.response.*
import com.kazemieh.domain.profile.*
import com.kazemieh.domain.address.*
import com.kazemieh.network.common.*
import com.kazemieh.common.*




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
