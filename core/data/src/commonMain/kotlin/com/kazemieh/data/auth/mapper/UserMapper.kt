package com.kazemieh.data.auth.mapper

import com.kazemieh.domain.model.Auth
import com.kazemieh.domain.model.Profile
import com.kazemieh.domain.model.User
import com.kazemieh.network.dto.response.AuthResponse
import com.kazemieh.network.dto.response.UserResponse


fun UserResponse.toProfile(): Profile {
    return Profile(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName,
        city = city,
        postalCode = postalCode,
        phone = phone,
        role = role,
    )
}


fun AuthResponse.toDomain(): Auth {
    return Auth(accessToken, refreshToken, user.toProfile())
}