package com.kazemieh.network.profile

import com.kazemieh.network.profile.dto.request.*
import com.kazemieh.network.profile.dto.response.*

import com.kazemieh.network.common.PageResponse

import com.kazemieh.network.profile.dto.response.ProfileResponse
import com.kazemieh.network.profile.dto.request.UpdateProfileRequest

interface ProfileApi {
    suspend fun getProfile(): ProfileResponse
    suspend fun updateProfile(request: UpdateProfileRequest): ProfileResponse
}
