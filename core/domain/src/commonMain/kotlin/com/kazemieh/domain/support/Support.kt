package com.kazemieh.domain.support

enum class SenderRole { CUSTOMER, ADMIN, UNKNOWN }

data class SupportMessage(
    val id: Long,
    val senderRole: SenderRole,
    val body: String,
    val createdAt: String
)

data class SupportTicket(
    val id: Long,
    val subject: String,
    val status: String,
    val lastMessage: String?,
    val createdAt: String,
    val updatedAt: String
)

data class SupportTicketDetail(
    val id: Long,
    val subject: String,
    val status: String,
    val messages: List<SupportMessage>,
    val createdAt: String,
    val updatedAt: String
)
