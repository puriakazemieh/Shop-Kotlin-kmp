package com.kazemieh.network.common

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val message: String,
    val status: String,
    val code: Int,
    val errorCode: String = "UNKNOWN_ERROR",
    val path: String? = null,
    val timestamp: String? = null
)

class ApiException(
    val messageText: Any,
    val code: Int?
) : Exception(if (messageText is String) messageText else "API Error")
