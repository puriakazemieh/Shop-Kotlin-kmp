package com.kazemieh.academy.placement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.academy.GetPlacementQuizUseCase
import com.kazemieh.domain.academy.PlacementQuizQuestion
import com.kazemieh.domain.academy.PlacementQuizResult
import com.kazemieh.domain.academy.SubmitPlacementQuizUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlacementQuizState(
    val isLoading: Boolean = false,
    val questions: List<PlacementQuizQuestion> = emptyList(),
    /** ایندکسِ گزینه‌ی انتخابی per ایندکسِ سؤال؛ -1 یعنی هنوز انتخاب نشده. */
    val answers: Map<Int, Int> = emptyMap(),
    val submitting: Boolean = false,
    val result: PlacementQuizResult? = null
)

class PlacementQuizViewModel(
    private val getPlacementQuizUseCase: GetPlacementQuizUseCase,
    private val submitPlacementQuizUseCase: SubmitPlacementQuizUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PlacementQuizState())
    val state: StateFlow<PlacementQuizState> = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = getPlacementQuizUseCase()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, questions = result.data) }
                else -> _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectAnswer(questionIndex: Int, optionIndex: Int) {
        _state.update { it.copy(answers = it.answers + (questionIndex to optionIndex)) }
    }

    fun submit() {
        val questions = _state.value.questions
        val answers = questions.indices.map { _state.value.answers[it] ?: 0 }
        _state.update { it.copy(submitting = true) }
        viewModelScope.launch {
            when (val result = submitPlacementQuizUseCase(answers)) {
                is AppResult.Success -> _state.update { it.copy(submitting = false, result = result.data) }
                else -> _state.update { it.copy(submitting = false) }
            }
        }
    }
}
