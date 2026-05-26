package com.kazemieh.network.address.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateAddressRequest(
    val receiverName: String,
    val receiverPhone: String,
    val country: String = "IR",
    val province: String,
    val city: String,
    val addressLine1: String,
    val addressLine2: String? = null,
    val postalCode: String? = null,
    val setAsDefault: Boolean = false,
)

@Serializable
data class UpdateAddressRequest(
    val receiverName: String? = null,
    val receiverPhone: String? = null,
    val country: String? = null,
    val province: String? = null,
    val city: String? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val postalCode: String? = null,
)
