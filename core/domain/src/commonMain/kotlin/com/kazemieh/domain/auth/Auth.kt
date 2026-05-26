package com.kazemieh.domain.auth

import com.kazemieh.domain.profile.Profile
import kotlinx.serialization.Serializable


@Serializable
data class Auth(
    val accessToken: String,
    val refreshToken: String,
    val profile: Profile,
)
