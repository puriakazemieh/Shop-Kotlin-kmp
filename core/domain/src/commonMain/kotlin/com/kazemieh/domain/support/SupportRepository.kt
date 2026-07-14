package com.kazemieh.domain.support

import com.kazemieh.common.AppResult

interface SupportRepository {
    suspend fun getTickets(): AppResult<List<SupportTicket>>
    suspend fun getTicket(ticketId: Long): AppResult<SupportTicketDetail>
    suspend fun createTicket(subject: String, message: String): AppResult<SupportTicketDetail>
    suspend fun postMessage(ticketId: Long, body: String): AppResult<SupportTicketDetail>
}
