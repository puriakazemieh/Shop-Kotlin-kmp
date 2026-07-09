package com.kazemieh.clinic.messaging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.clinic.ClinicMessage
import com.kazemieh.domain.clinic.GetClinicMessagesUseCase
import com.kazemieh.domain.clinic.GetMessagingStatusUseCase
import com.kazemieh.domain.clinic.MessagingPlanStatus
import com.kazemieh.domain.clinic.SendClinicMessageUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MessagingState(
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val messages: List<ClinicMessage> = emptyList(),
    val status: MessagingPlanStatus? = null
)

sealed interface MessagingEffect {
    data class ShowError(val message: Any) : MessagingEffect
}

class MessagingViewModel(
    private val getClinicMessagesUseCase: GetClinicMessagesUseCase,
    private val sendClinicMessageUseCase: SendClinicMessageUseCase,
    private val getMessagingStatusUseCase: GetMessagingStatusUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MessagingState())
    val state: StateFlow<MessagingState> = _state.asStateFlow()

    private val _effect = Channel<MessagingEffect>()
    val effect: Flow<MessagingEffect> = _effect.receiveAsFlow()

    private var therapistId: Long = 0

    fun load(therapistId: Long) {
        this.therapistId = therapistId
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = getClinicMessagesUseCase(therapistId)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, messages = result.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(MessagingEffect.ShowError(result.message))
                }
                else -> {}
            }
            when (val statusResult = getMessagingStatusUseCase(therapistId)) {
                is AppResult.Success -> _state.update { it.copy(status = statusResult.data) }
                else -> {}
            }
        }
    }

    fun send(body: String) {
        if (body.isBlank() || _state.value.isSending) return
        _state.update { it.copy(isSending = true) }
        viewModelScope.launch {
            when (val result = sendClinicMessageUseCase(therapistId, body.trim())) {
                is AppResult.Success -> {
                    _state.update { it.copy(isSending = false, messages = it.messages + result.data) }
                    when (val statusResult = getMessagingStatusUseCase(therapistId)) {
                        is AppResult.Success -> _state.update { it.copy(status = statusResult.data) }
                        else -> {}
                    }
                }
                is AppResult.Error -> {
                    _state.update { it.copy(isSending = false) }
                    _effect.send(MessagingEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }
}
