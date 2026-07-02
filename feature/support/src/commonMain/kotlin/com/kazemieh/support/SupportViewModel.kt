package com.kazemieh.support

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.support.CreateTicketUseCase
import com.kazemieh.domain.support.GetTicketDetailUseCase
import com.kazemieh.domain.support.GetTicketsUseCase
import com.kazemieh.domain.support.PostSupportMessageUseCase
import com.kazemieh.domain.support.SupportTicketDetail
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * چتِ پشتیبانی — یک گفتگوی واحد. آخرین تیکت را باز می‌کند؛ اگر تیکتی نبود،
 * اولین پیام یک تیکتِ جدید می‌سازد. پیام‌های ادمین/مشتری به‌صورتِ حباب نشان داده می‌شوند.
 */
class SupportViewModel(
    private val getTicketsUseCase: GetTicketsUseCase,
    private val getTicketDetailUseCase: GetTicketDetailUseCase,
    private val createTicketUseCase: CreateTicketUseCase,
    private val postSupportMessageUseCase: PostSupportMessageUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SupportState())
    val state: StateFlow<SupportState> = _state.asStateFlow()

    private val _effect = Channel<SupportEffect>()
    val effect: Flow<SupportEffect> = _effect.receiveAsFlow()

    init {
        load()
    }

    fun handleIntent(intent: SupportIntent) {
        when (intent) {
            is SupportIntent.Load -> load()
            is SupportIntent.UpdateInput -> _state.update { it.copy(input = intent.text) }
            is SupportIntent.Send -> send()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = getTicketsUseCase()) {
                is AppResult.Success -> {
                    val latest = result.data.firstOrNull()
                    if (latest == null) {
                        _state.update { it.copy(isLoading = false, activeTicketId = null, messages = emptyList()) }
                    } else {
                        loadTicket(latest.id)
                    }
                }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    private suspend fun loadTicket(ticketId: Long) {
        when (val result = getTicketDetailUseCase(ticketId)) {
            is AppResult.Success -> applyDetail(result.data)
            is AppResult.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
            else -> {}
        }
    }

    private fun applyDetail(detail: SupportTicketDetail) {
        _state.update {
            it.copy(
                isLoading = false,
                isSending = false,
                activeTicketId = detail.id,
                subject = detail.subject,
                status = detail.status,
                messages = detail.messages,
                error = null
            )
        }
    }

    private fun send() {
        val text = _state.value.input.trim()
        if (text.isEmpty() || _state.value.isSending) return
        val ticketId = _state.value.activeTicketId
        viewModelScope.launch {
            _state.update { it.copy(isSending = true, input = "") }
            val result = if (ticketId == null) {
                createTicketUseCase(subject = "پشتیبانی", message = text)
            } else {
                postSupportMessageUseCase(ticketId, text)
            }
            when (result) {
                is AppResult.Success -> applyDetail(result.data)
                is AppResult.Error -> {
                    _state.update { it.copy(isSending = false, input = text, error = result.message) }
                    _effect.send(SupportEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }
}
