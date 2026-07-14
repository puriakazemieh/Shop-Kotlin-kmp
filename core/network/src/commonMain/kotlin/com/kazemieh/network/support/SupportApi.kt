package com.kazemieh.network.support

import com.kazemieh.network.support.dto.*

interface SupportApi {
    suspend fun getTickets(): List<SupportTicketResponse>
    suspend fun getTicket(ticketId: Long): SupportTicketDetailResponse
    suspend fun createTicket(request: CreateTicketRequestDto): SupportTicketDetailResponse
    suspend fun postMessage(ticketId: Long, request: PostMessageRequestDto): SupportTicketDetailResponse
}
