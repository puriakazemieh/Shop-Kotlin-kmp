package com.kazemieh.network.order.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class AddressSnapshotResponse(
    val receiverName: String,
    val receiverPhone: String,
    val country: String,
    val province: String,
    val city: String,
    val addressLine1: String,
    val addressLine2: String?,
    val postalCode: String?,
)
