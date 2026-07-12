package com.kazemieh.academy.lessonquiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.academy.GetLessonQuizUseCase
import com.kazemieh.domain.academy.LessonQuiz
import com.kazemieh.domain.academy.LessonQuizResult
import com.kazemieh.domain.academy.SubmitLessonQuizUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LessonQuizState(
    val isLoading: Boolean = false,
    val quiz: LessonQuiz? = null,
    val answers: Map<Int, Int> = emptyMap(),
    val submitting: Boolean = false,
    val result: LessonQuizResult? = null
)

sealed interface LessonQuizEffect {
    data class ShowError(val message: Any) : LessonQuizEffect
}

class LessonQuizViewModel(
    private val getLessonQuizUseCase: GetLessonQuizUseCase,
    private val submitLessonQuizUseCase: SubmitLessonQuizUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LessonQuizState())
    val state: StateFlow<LessonQuizState> = _state.asStateFlow()

    private val _effect = Channel<LessonQuizEffect>()
    val effect: Flow<LessonQuizEffect> = _effect.receiveAsFlow()

    private var lessonId: Long = 0

    fun load(lessonId: Long) {
        this.lessonId = lessonId
        _state.update { it.copy(isLoading = true, result = null, answers = emptyMap()) }
        viewModelScope.launch {
            when (val r = getLessonQuizUseCase(lessonId)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, quiz = r.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(LessonQuizEffect.ShowError(r.message))
                }
                else -> {}
            }
        }
    }

    fun selectAnswer(questionIndex: Int, optionIndex: Int) {
        _state.update { it.copy(answers = it.answers + (questionIndex to optionIndex)) }
    }

    fun submit() {
        _state.update { it.copy(submitting = true) }
        viewModelScope.launch {
            when (val r = submitLessonQuizUseCase(lessonId, _state.value.answers)) {
                is AppResult.Success -> _state.update { it.copy(submitting = false, result = r.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(submitting = false) }
                    _effect.send(LessonQuizEffect.ShowError(r.message))
                }
                else -> {}
            }
        }
    }

    fun allAnswered(): Boolean {
        val quiz = _state.value.quiz ?: return false
        return quiz.questions.all { _state.value.answers.containsKey(it.index) }
    }
}
