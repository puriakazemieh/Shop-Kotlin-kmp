package com.kazemieh.data.profile.mapper

import com.kazemieh.domain.model.Profile
import com.kazemieh.network.dto.response.ProfileResponse
import com.kazemieh.network.dto.request.UpdateProfileRequest

fun ProfileResponse.toDomain(): Profile {
    return Profile(
        id = id,
        email = email,
        firstName = firstName ?: "",
        lastName = lastName ?: "",
        phone = phone,
        city = city,
        role = role,
        postalCode = postalCode,
    )
}

fun Profile.toUpdateRequest(): UpdateProfileRequest {
    return UpdateProfileRequest(
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        city = city,
        postalCode = postalCode,
    )
}

