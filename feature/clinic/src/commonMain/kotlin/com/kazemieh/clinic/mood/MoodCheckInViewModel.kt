package com.kazemieh.clinic.mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.clinic.GetMoodHistoryUseCase
import com.kazemieh.domain.clinic.MoodCheckIn
import com.kazemieh.domain.clinic.SubmitMoodCheckInUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MoodCheckInState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val history: List<MoodCheckIn> = emptyList(),
    val error: Any? = null
)

sealed interface MoodCheckInEffect {
    data class ShowError(val message: Any) : MoodCheckInEffect
    data object Submitted : MoodCheckInEffect
}

class MoodCheckInViewModel(
    private val submitMoodCheckInUseCase: SubmitMoodCheckInUseCase,
    private val getMoodHistoryUseCase: GetMoodHistoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MoodCheckInState())
    val state: StateFlow<MoodCheckInState> = _state.asStateFlow()

    private val _effect = Channel<MoodCheckInEffect>()
    val effect: Flow<MoodCheckInEffect> = _effect.receiveAsFlow()

    fun load() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = getMoodHistoryUseCase()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, history = result.data, error = null) }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                    _effect.send(MoodCheckInEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    fun submit(moodScore: Int, note: String?) {
        if (_state.value.isSubmitting) return
        _state.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            when (val result = submitMoodCheckInUseCase(moodScore, note)) {
                is AppResult.Success -> {
                    _state.update { it.copy(isSubmitting = false) }
                    _effect.send(MoodCheckInEffect.Submitted)
                    load()
                }
                is AppResult.Error -> {
                    _state.update { it.copy(isSubmitting = false) }
                    _effect.send(MoodCheckInEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }
}
