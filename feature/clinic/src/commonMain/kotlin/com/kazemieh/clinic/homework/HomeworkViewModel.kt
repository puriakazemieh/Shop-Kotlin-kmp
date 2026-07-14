package com.kazemieh.clinic.homework

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.clinic.CompleteHomeworkUseCase
import com.kazemieh.domain.clinic.GetMyHomeworkUseCase
import com.kazemieh.domain.clinic.Homework
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeworkState(
    val isLoading: Boolean = false,
    val homework: List<Homework> = emptyList(),
    val completingId: Long? = null
)

sealed interface HomeworkEffect {
    data class ShowError(val message: Any) : HomeworkEffect
}

class HomeworkViewModel(
    private val getMyHomeworkUseCase: GetMyHomeworkUseCase,
    private val completeHomeworkUseCase: CompleteHomeworkUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeworkState())
    val state: StateFlow<HomeworkState> = _state.asStateFlow()

    private val _effect = Channel<HomeworkEffect>()
    val effect: Flow<HomeworkEffect> = _effect.receiveAsFlow()

    fun load() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = getMyHomeworkUseCase()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, homework = result.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(HomeworkEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    fun complete(id: Long) {
        if (_state.value.completingId != null) return
        _state.update { it.copy(completingId = id) }
        viewModelScope.launch {
            when (val result = completeHomeworkUseCase(id)) {
                is AppResult.Success -> {
                    _state.update { it.copy(completingId = null) }
                    load()
                }
                is AppResult.Error -> {
                    _state.update { it.copy(completingId = null) }
                    _effect.send(HomeworkEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }
}
