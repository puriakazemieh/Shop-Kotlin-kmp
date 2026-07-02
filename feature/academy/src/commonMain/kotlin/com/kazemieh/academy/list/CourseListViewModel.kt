package com.kazemieh.academy.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.academy.CourseSummary
import com.kazemieh.domain.academy.GetCoursesUseCase
import com.kazemieh.domain.academy.GetMyCoursesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CourseListState(
    val isLoading: Boolean = false,
    val courses: List<CourseSummary> = emptyList(),
    val mine: Boolean = false,
    val error: Any? = null
)

class CourseListViewModel(
    private val getCoursesUseCase: GetCoursesUseCase,
    private val getMyCoursesUseCase: GetMyCoursesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CourseListState())
    val state: StateFlow<CourseListState> = _state.asStateFlow()

    fun load(mine: Boolean) {
        _state.update { it.copy(isLoading = true, mine = mine) }
        viewModelScope.launch {
            val result = if (mine) getMyCoursesUseCase() else getCoursesUseCase()
            when (result) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, courses = result.data, error = null) }
                is AppResult.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                else -> {}
            }
        }
    }
}
