package com.kazemieh.clinic.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.clinic.GetSessionReceiptUseCase
import com.kazemieh.domain.clinic.SessionReceipt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SessionReceiptState(
    val isLoading: Boolean = false,
    val receipt: SessionReceipt? = null,
    val error: Any? = null
)

class SessionReceiptViewModel(
    private val getSessionReceiptUseCase: GetSessionReceiptUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SessionReceiptState())
    val state: StateFlow<SessionReceiptState> = _state.asStateFlow()

    fun load(appointmentId: Long) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = getSessionReceiptUseCase(appointmentId)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, receipt = result.data, error = null) }
                is AppResult.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                else -> {}
            }
        }
    }
}
