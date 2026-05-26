package com.kazemieh.domain.address

import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val id: Long,
    val receiverName: String,
    val receiverPhone: String,
    val country: String,
    val province: String,
    val city: String,
    val addressLine1: String,
    val addressLine2: String?,
    val postalCode: String?,
    val isDefault: Boolean,
)
