package com.kazemieh.data.support.repository

import com.kazemieh.common.AppResult
import com.kazemieh.domain.support.SenderRole
import com.kazemieh.domain.support.SupportMessage
import com.kazemieh.domain.support.SupportRepository
import com.kazemieh.domain.support.SupportTicket
import com.kazemieh.domain.support.SupportTicketDetail
import com.kazemieh.network.common.safeApiCall
import com.kazemieh.network.support.SupportApi
import com.kazemieh.network.support.dto.CreateTicketRequestDto
import com.kazemieh.network.support.dto.PostMessageRequestDto
import com.kazemieh.network.support.dto.SupportMessageResponse
import com.kazemieh.network.support.dto.SupportTicketDetailResponse
import com.kazemieh.network.support.dto.SupportTicketResponse

class SupportRepositoryImpl(
    private val api: SupportApi
) : SupportRepository {

    override suspend fun getTickets(): AppResult<List<SupportTicket>> = safeApiCall {
        api.getTickets().map { it.toDomain() }
    }

    override suspend fun getTicket(ticketId: Long): AppResult<SupportTicketDetail> = safeApiCall {
        api.getTicket(ticketId).toDomain()
    }

    override suspend fun createTicket(subject: String, message: String): AppResult<SupportTicketDetail> = safeApiCall {
        api.createTicket(CreateTicketRequestDto(subject = subject, message = message)).toDomain()
    }

    override suspend fun postMessage(ticketId: Long, body: String): AppResult<SupportTicketDetail> = safeApiCall {
        api.postMessage(ticketId, PostMessageRequestDto(body = body)).toDomain()
    }

    private fun SupportTicketResponse.toDomain() = SupportTicket(
        id = id,
        subject = subject,
        status = status,
        lastMessage = lastMessage,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun SupportTicketDetailResponse.toDomain() = SupportTicketDetail(
        id = id,
        subject = subject,
        status = status,
        messages = messages.map { it.toDomain() },
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun SupportMessageResponse.toDomain() = SupportMessage(
        id = id,
        senderRole = when (senderRole.uppercase()) {
            "CUSTOMER" -> SenderRole.CUSTOMER
            "ADMIN" -> SenderRole.ADMIN
            else -> SenderRole.UNKNOWN
        },
        body = body,
        createdAt = createdAt
    )
}
