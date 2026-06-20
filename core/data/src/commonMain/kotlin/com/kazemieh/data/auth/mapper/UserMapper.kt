package com.kazemieh.data.auth.mapper

import com.kazemieh.network.auth.dto.request.*
import com.kazemieh.network.auth.dto.response.*
import com.kazemieh.network.profile.dto.response.UserResponse
import com.kazemieh.domain.auth.*
import com.kazemieh.domain.profile.Profile
import com.kazemieh.network.common.*
import com.kazemieh.common.*





fun UserResponse.toProfile(): Profile {
    return Profile(
        id = id,
        email = email,
        mobile = mobile,
        fullName = fullName ?: "${firstName.orEmpty()} ${lastName.orEmpty()}".trim().ifEmpty { null },
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        city = city,
        postalCode = postalCode,
        role = role,
    )
}


fun AuthResponse.toDomain(): Auth {
    return Auth(accessToken, refreshToken, user.toProfile())
}
