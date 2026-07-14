package com.kazemieh.academy.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.academy.GetQuizUseCase
import com.kazemieh.domain.academy.Quiz
import com.kazemieh.domain.academy.QuizResult
import com.kazemieh.domain.academy.SubmitQuizUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CourseQuizState(
    val isLoading: Boolean = false,
    val quiz: Quiz? = null,
    /** questionIndex -> selectedOptionIndex */
    val answers: Map<Int, Int> = emptyMap(),
    val submitting: Boolean = false,
    val result: QuizResult? = null
)

sealed interface CourseQuizEffect {
    data class ShowError(val message: Any) : CourseQuizEffect
}

class CourseQuizViewModel(
    private val getQuizUseCase: GetQuizUseCase,
    private val submitQuizUseCase: SubmitQuizUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CourseQuizState())
    val state: StateFlow<CourseQuizState> = _state.asStateFlow()

    private val _effect = Channel<CourseQuizEffect>()
    val effect: Flow<CourseQuizEffect> = _effect.receiveAsFlow()

    private var courseId: Long = 0

    fun load(courseId: Long) {
        this.courseId = courseId
        _state.update { it.copy(isLoading = true, result = null, answers = emptyMap()) }
        viewModelScope.launch {
            when (val r = getQuizUseCase(courseId)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, quiz = r.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(CourseQuizEffect.ShowError(r.message))
                }
                else -> {}
            }
        }
    }

    fun selectAnswer(questionIndex: Int, optionIndex: Int) {
        _state.update { it.copy(answers = it.answers + (questionIndex to optionIndex)) }
    }

    fun submit() {
        val quiz = _state.value.quiz ?: return
        _state.update { it.copy(submitting = true) }
        viewModelScope.launch {
            when (val r = submitQuizUseCase(courseId, _state.value.answers)) {
                is AppResult.Success -> _state.update { it.copy(submitting = false, result = r.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(submitting = false) }
                    _effect.send(CourseQuizEffect.ShowError(r.message))
                }
                else -> {}
            }
        }
    }

    /** آیا همه‌ی سؤالات پاسخ داده شده‌اند؟ */
    fun allAnswered(): Boolean {
        val quiz = _state.value.quiz ?: return false
        return quiz.questions.all { _state.value.answers.containsKey(it.index) }
    }
}
