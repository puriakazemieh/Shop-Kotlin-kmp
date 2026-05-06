package com.kazemieh.network.dto

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

@Serializable
data class ApiException(
    val messageText: String,
    val code: Int?
) : Exception(messageText)
