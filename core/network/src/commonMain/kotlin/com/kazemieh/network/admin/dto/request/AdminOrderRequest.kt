package com.kazemieh.network.admin.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminUpdateOrderStatusRequest(
    val status: String
)
