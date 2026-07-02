package com.kazemieh.domain.support

class GetTicketsUseCase(private val repository: SupportRepository) {
    suspend operator fun invoke() = repository.getTickets()
}

class GetTicketDetailUseCase(private val repository: SupportRepository) {
    suspend operator fun invoke(ticketId: Long) = repository.getTicket(ticketId)
}

class CreateTicketUseCase(private val repository: SupportRepository) {
    suspend operator fun invoke(subject: String, message: String) =
        repository.createTicket(subject, message)
}

class PostSupportMessageUseCase(private val repository: SupportRepository) {
    suspend operator fun invoke(ticketId: Long, body: String) =
        repository.postMessage(ticketId, body)
}
