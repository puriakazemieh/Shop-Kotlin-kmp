package com.kazemieh.domain.order

data class ReturnRequest(
    val id: Long,
    val orderId: Long,
    val orderItemId: Long,
    val itemTitle: String,
    val type: String,
    val reason: String,
    val status: String,
    val adminNote: String?,
    val createdAt: String?,
    val resolvedAt: String?
)

data class AdminReturnRequest(
    val id: Long,
    val orderId: Long,
    val orderItemId: Long,
    val itemTitle: String,
    val userId: Long,
    val userName: String?,
    val type: String,
    val reason: String,
    val status: String,
    val adminNote: String?,
    val createdAt: String?,
    val resolvedAt: String?
)
