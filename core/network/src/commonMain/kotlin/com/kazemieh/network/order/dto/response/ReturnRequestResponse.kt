package com.kazemieh.network.order.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class ReturnRequestResponse(
    val id: Long,
    val orderId: Long,
    val orderItemId: Long,
    val itemTitle: String,
    val type: String,
    val reason: String,
    val status: String,
    val adminNote: String? = null,
    val createdAt: String? = null,
    val resolvedAt: String? = null
)

@Serializable
data class AdminReturnRequestResponse(
    val id: Long,
    val orderId: Long,
    val orderItemId: Long,
    val itemTitle: String,
    val userId: Long,
    val userName: String? = null,
    val type: String,
    val reason: String,
    val status: String,
    val adminNote: String? = null,
    val createdAt: String? = null,
    val resolvedAt: String? = null
)
