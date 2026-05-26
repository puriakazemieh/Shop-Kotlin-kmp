package com.kazemieh.data.profile.source

import com.kazemieh.network.profile.dto.request.*
import com.kazemieh.network.profile.dto.response.*
import com.kazemieh.network.address.dto.request.*
import com.kazemieh.network.address.dto.response.*
import com.kazemieh.domain.profile.*
import com.kazemieh.domain.address.*
import com.kazemieh.network.common.*
import com.kazemieh.common.*




interface ProfileDataSource {
    suspend fun getProfile(): AppResult<Profile>
    suspend fun updateProfile(request: Profile): AppResult<Profile>
}
