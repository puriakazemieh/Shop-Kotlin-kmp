package com.kazemieh.network.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddressResponse(
    val id: Long,
    val receiverName: String,
    val receiverPhone: String,
    val country: String,
    val province: String,
    val city: String,
    val addressLine1: String,
    val addressLine2: String?,
    val postalCode: String?,
    @SerialName("default")
    val isDefault: Boolean,
    val createdAt: String?,
)
