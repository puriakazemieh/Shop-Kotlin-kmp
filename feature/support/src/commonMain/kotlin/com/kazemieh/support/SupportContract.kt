package com.kazemieh.support

import com.kazemieh.domain.support.SupportMessage

data class SupportState(
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val activeTicketId: Long? = null,
    val subject: String = "",
    val status: String? = null,
    val messages: List<SupportMessage> = emptyList(),
    val input: String = "",
    val error: Any? = null
)

sealed interface SupportIntent {
    data object Load : SupportIntent
    data class UpdateInput(val text: String) : SupportIntent
    data object Send : SupportIntent
}

sealed interface SupportEffect {
    data class ShowError(val message: Any) : SupportEffect
}
