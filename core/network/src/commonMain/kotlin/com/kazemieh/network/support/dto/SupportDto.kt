package com.kazemieh.network.support.dto

import kotlinx.serialization.Serializable

@Serializable
data class SupportMessageResponse(
    val id: Long,
    val senderRole: String,
    val body: String,
    val createdAt: String
)

@Serializable
data class SupportTicketResponse(
    val id: Long,
    val subject: String,
    val status: String,
    val lastMessage: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class SupportTicketDetailResponse(
    val id: Long,
    val subject: String,
    val status: String,
    val userId: Long,
    val messages: List<SupportMessageResponse>,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CreateTicketRequestDto(
    val subject: String,
    val message: String
)

@Serializable
data class PostMessageRequestDto(
    val body: String
)
