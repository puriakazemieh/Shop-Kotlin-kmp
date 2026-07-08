package com.kazemieh.clinic.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.clinic.BookAppointmentUseCase
import com.kazemieh.domain.clinic.GetTherapistDetailUseCase
import com.kazemieh.domain.clinic.RequestTherapistSwitchUseCase
import com.kazemieh.domain.clinic.TherapistDetail
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TherapistDetailState(
    val isLoading: Boolean = false,
    val bookingSlotId: Long? = null,
    val therapist: TherapistDetail? = null,
    val isRequestingSwitch: Boolean = false,
    val error: Any? = null
)

sealed interface TherapistDetailEffect {
    data class ShowError(val message: Any) : TherapistDetailEffect
    data object Booked : TherapistDetailEffect
    data object SwitchRequested : TherapistDetailEffect
}

class TherapistDetailViewModel(
    private val getTherapistDetailUseCase: GetTherapistDetailUseCase,
    private val bookAppointmentUseCase: BookAppointmentUseCase,
    private val requestTherapistSwitchUseCase: RequestTherapistSwitchUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TherapistDetailState())
    val state: StateFlow<TherapistDetailState> = _state.asStateFlow()

    private val _effect = Channel<TherapistDetailEffect>()
    val effect: Flow<TherapistDetailEffect> = _effect.receiveAsFlow()

    private var currentSlug: String? = null

    fun load(slug: String) {
        currentSlug = slug
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = getTherapistDetailUseCase(slug)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, therapist = result.data, error = null) }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                    _effect.send(TherapistDetailEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    fun book(slotId: Long) {
        if (_state.value.bookingSlotId != null) return
        _state.update { it.copy(bookingSlotId = slotId) }
        viewModelScope.launch {
            when (val result = bookAppointmentUseCase(slotId)) {
                is AppResult.Success -> {
                    _state.update { it.copy(bookingSlotId = null) }
                    _effect.send(TherapistDetailEffect.Booked)
                    currentSlug?.let { load(it) } // رفرشِ بازه‌ها (بازه‌ی رزروشده حذف می‌شود)
                }
                is AppResult.Error -> {
                    _state.update { it.copy(bookingSlotId = null) }
                    _effect.send(TherapistDetailEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    /** درخواستِ تعویضِ همینِ درمانگر با درمانگرِ دیگر (بدونِ مقصدِ مشخص — ادمین تصمیم می‌گیرد). */
    fun requestSwitch(reason: String?) {
        val therapistId = _state.value.therapist?.id ?: return
        if (_state.value.isRequestingSwitch) return
        _state.update { it.copy(isRequestingSwitch = true) }
        viewModelScope.launch {
            when (val result = requestTherapistSwitchUseCase(therapistId, null, reason)) {
                is AppResult.Success -> {
                    _state.update { it.copy(isRequestingSwitch = false) }
                    _effect.send(TherapistDetailEffect.SwitchRequested)
                }
                is AppResult.Error -> {
                    _state.update { it.copy(isRequestingSwitch = false) }
                    _effect.send(TherapistDetailEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }
}
