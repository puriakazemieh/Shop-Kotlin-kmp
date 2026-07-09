package com.kazemieh.clinic.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.clinic.GetTherapistMatchQuestionsUseCase
import com.kazemieh.domain.clinic.SubmitTherapistMatchUseCase
import com.kazemieh.domain.clinic.TherapistMatchQuestion
import com.kazemieh.domain.clinic.TherapistMatchResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TherapistMatchState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val questions: List<TherapistMatchQuestion> = emptyList(),
    val selectedTags: Set<String> = emptySet(),
    val results: List<TherapistMatchResult>? = null
)

sealed interface TherapistMatchEffect {
    data class ShowError(val message: Any) : TherapistMatchEffect
}

class TherapistMatchViewModel(
    private val getTherapistMatchQuestionsUseCase: GetTherapistMatchQuestionsUseCase,
    private val submitTherapistMatchUseCase: SubmitTherapistMatchUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TherapistMatchState())
    val state: StateFlow<TherapistMatchState> = _state.asStateFlow()

    private val _effect = Channel<TherapistMatchEffect>()
    val effect: Flow<TherapistMatchEffect> = _effect.receiveAsFlow()

    fun load() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = getTherapistMatchQuestionsUseCase()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, questions = result.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(TherapistMatchEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    fun toggleTag(tag: String) {
        _state.update {
            val next = if (tag in it.selectedTags) it.selectedTags - tag else it.selectedTags + tag
            it.copy(selectedTags = next)
        }
    }

    fun submit() {
        if (_state.value.selectedTags.isEmpty() || _state.value.isSubmitting) return
        _state.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            when (val result = submitTherapistMatchUseCase(_state.value.selectedTags.toList())) {
                is AppResult.Success -> _state.update { it.copy(isSubmitting = false, results = result.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(isSubmitting = false) }
                    _effect.send(TherapistMatchEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }
}
