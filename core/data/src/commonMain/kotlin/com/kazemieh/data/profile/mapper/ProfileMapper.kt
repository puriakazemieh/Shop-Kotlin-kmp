package com.kazemieh.data.profile.mapper

import com.kazemieh.network.profile.dto.request.*
import com.kazemieh.network.profile.dto.response.*
import com.kazemieh.network.address.dto.request.*
import com.kazemieh.network.address.dto.response.*
import com.kazemieh.domain.profile.*
import com.kazemieh.domain.address.*
import com.kazemieh.network.common.*
import com.kazemieh.common.*



fun ProfileResponse.toDomain(): Profile {
    return Profile(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        mobile = phone,
        fullName = if (firstName != null && lastName != null) "$firstName $lastName" else firstName ?: lastName,
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

