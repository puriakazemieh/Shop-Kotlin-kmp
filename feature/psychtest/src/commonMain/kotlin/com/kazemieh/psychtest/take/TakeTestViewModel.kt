package com.kazemieh.psychtest.take

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.psychtest.GetUserTestQuestionsUseCase
import com.kazemieh.domain.psychtest.PsychTestDetail
import com.kazemieh.domain.psychtest.SubmitPsychTestUseCase
import com.kazemieh.domain.psychtest.UserPsychTest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TakeTestState(
    val isLoading: Boolean = false,
    val test: PsychTestDetail? = null,
    val answers: Map<Int, Int> = emptyMap(),
    val submitting: Boolean = false,
    val result: UserPsychTest? = null
)

sealed interface TakeTestEffect {
    data class ShowError(val message: Any) : TakeTestEffect
}

class TakeTestViewModel(
    private val getUserTestQuestionsUseCase: GetUserTestQuestionsUseCase,
    private val submitPsychTestUseCase: SubmitPsychTestUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TakeTestState())
    val state: StateFlow<TakeTestState> = _state.asStateFlow()

    private val _effect = Channel<TakeTestEffect>()
    val effect: Flow<TakeTestEffect> = _effect.receiveAsFlow()

    private var userTestId: Long = 0

    fun load(userTestId: Long) {
        this.userTestId = userTestId
        _state.update { it.copy(isLoading = true, result = null, answers = emptyMap()) }
        viewModelScope.launch {
            when (val r = getUserTestQuestionsUseCase(userTestId)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, test = r.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(TakeTestEffect.ShowError(r.message))
                }
                else -> {}
            }
        }
    }

    fun selectAnswer(questionIndex: Int, optionIndex: Int) {
        _state.update { it.copy(answers = it.answers + (questionIndex to optionIndex)) }
    }

    fun allAnswered(): Boolean {
        val test = _state.value.test ?: return false
        return test.questions.all { _state.value.answers.containsKey(it.index) }
    }

    fun submit() {
        _state.update { it.copy(submitting = true) }
        viewModelScope.launch {
            when (val r = submitPsychTestUseCase(userTestId, _state.value.answers)) {
                is AppResult.Success -> _state.update { it.copy(submitting = false, result = r.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(submitting = false) }
                    _effect.send(TakeTestEffect.ShowError(r.message))
                }
                else -> {}
            }
        }
    }
}
