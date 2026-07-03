package com.kazemieh.clinic.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.clinic.Appointment
import com.kazemieh.domain.clinic.CancelAppointmentUseCase
import com.kazemieh.domain.clinic.GetMyAppointmentsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyAppointmentsState(
    val isLoading: Boolean = false,
    val appointments: List<Appointment> = emptyList(),
    val cancellingId: Long? = null,
    val error: Any? = null
)

sealed interface MyAppointmentsEffect {
    data class ShowError(val message: Any) : MyAppointmentsEffect
}

class MyAppointmentsViewModel(
    private val getMyAppointmentsUseCase: GetMyAppointmentsUseCase,
    private val cancelAppointmentUseCase: CancelAppointmentUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MyAppointmentsState())
    val state: StateFlow<MyAppointmentsState> = _state.asStateFlow()

    private val _effect = Channel<MyAppointmentsEffect>()
    val effect: Flow<MyAppointmentsEffect> = _effect.receiveAsFlow()

    fun load() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = getMyAppointmentsUseCase()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, appointments = result.data, error = null) }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                    _effect.send(MyAppointmentsEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    fun cancel(appointmentId: Long) {
        if (_state.value.cancellingId != null) return
        _state.update { it.copy(cancellingId = appointmentId) }
        viewModelScope.launch {
            when (val result = cancelAppointmentUseCase(appointmentId)) {
                is AppResult.Success -> {
                    _state.update { it.copy(cancellingId = null) }
                    load()
                }
                is AppResult.Error -> {
                    _state.update { it.copy(cancellingId = null) }
                    _effect.send(MyAppointmentsEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }
}
