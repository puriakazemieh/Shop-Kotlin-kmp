package com.kazemieh.domain.model

import kotlinx.serialization.Serializable


@Serializable
data class Auth(
    val accessToken: String,
    val refreshToken: String,
    val profile: Profile,
)