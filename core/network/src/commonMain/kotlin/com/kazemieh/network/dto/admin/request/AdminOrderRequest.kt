package com.kazemieh.network.dto.admin.request

import kotlinx.serialization.Serializable

@Serializable
data class AdminUpdateOrderStatusRequest(
    val status: String
)
