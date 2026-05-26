package com.kazemieh.domain.profile

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: Long,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val phone: String?,
    val city: String?,
    val role: String,
    val postalCode: Int?,
)
