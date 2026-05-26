package com.kazemieh.domain.profile


import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val city: String?,
    val postalCode: Int?,
    val phone: String?,
    val role: String,
    val isActive: Boolean,
)
