package com.kazemieh.network.order.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateReturnRequestRequest(
    val orderItemId: Long,
    val type: String,
    val reason: String
)

@Serializable
data class AdminUpdateReturnRequestRequest(
    val status: String,
    val adminNote: String? = null
)
