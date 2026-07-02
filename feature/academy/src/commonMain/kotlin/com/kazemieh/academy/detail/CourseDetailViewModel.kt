package com.kazemieh.academy.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.academy.CourseDetail
import com.kazemieh.domain.academy.EnrollCourseUseCase
import com.kazemieh.domain.academy.GetCourseDetailUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CourseDetailState(
    val isLoading: Boolean = false,
    val isEnrolling: Boolean = false,
    val course: CourseDetail? = null,
    val error: Any? = null
)

sealed interface CourseDetailEffect {
    data class ShowError(val message: Any) : CourseDetailEffect
}

class CourseDetailViewModel(
    private val getCourseDetailUseCase: GetCourseDetailUseCase,
    private val enrollCourseUseCase: EnrollCourseUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CourseDetailState())
    val state: StateFlow<CourseDetailState> = _state.asStateFlow()

    private val _effect = Channel<CourseDetailEffect>()
    val effect: Flow<CourseDetailEffect> = _effect.receiveAsFlow()

    fun load(slug: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = getCourseDetailUseCase(slug)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, course = result.data, error = null) }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                    _effect.send(CourseDetailEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    fun enroll() {
        val course = _state.value.course ?: return
        _state.update { it.copy(isEnrolling = true) }
        viewModelScope.launch {
            when (val result = enrollCourseUseCase(course.id)) {
                is AppResult.Success -> _state.update { it.copy(isEnrolling = false, course = result.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(isEnrolling = false) }
                    _effect.send(CourseDetailEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }
}
