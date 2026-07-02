package com.kazemieh.network.support

import com.kazemieh.network.support.dto.*
import com.kazemieh.network.common.safeApiCallRaw
import io.ktor.client.*
import io.ktor.client.request.*

class SupportApiImpl(
    private val client: HttpClient
) : SupportApi {

    override suspend fun getTickets(): List<SupportTicketResponse> = safeApiCallRaw {
        client.get("api/support/tickets")
    }

    override suspend fun getTicket(ticketId: Long): SupportTicketDetailResponse = safeApiCallRaw {
        client.get("api/support/tickets/$ticketId")
    }

    override suspend fun createTicket(request: CreateTicketRequestDto): SupportTicketDetailResponse = safeApiCallRaw {
        client.post("api/support/tickets") {
            setBody(request)
        }
    }

    override suspend fun postMessage(ticketId: Long, request: PostMessageRequestDto): SupportTicketDetailResponse = safeApiCallRaw {
        client.post("api/support/tickets/$ticketId/messages") {
            setBody(request)
        }
    }
}
