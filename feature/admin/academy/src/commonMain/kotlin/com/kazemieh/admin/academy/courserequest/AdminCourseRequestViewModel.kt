package com.kazemieh.admin.academy.courserequest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.courserequest.CourseRequest
import com.kazemieh.domain.courserequest.DeleteCourseRequestUseCase
import com.kazemieh.domain.courserequest.GetAdminCourseRequestsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminCourseRequestState(
    val requests: AppResult<List<CourseRequest>> = AppResult.Loading
)

sealed interface AdminCourseRequestEffect {
    data class ShowError(val message: Any) : AdminCourseRequestEffect
    data class ShowSuccess(val message: String) : AdminCourseRequestEffect
}

class AdminCourseRequestViewModel(
    private val getAdminCourseRequestsUseCase: GetAdminCourseRequestsUseCase,
    private val deleteCourseRequestUseCase: DeleteCourseRequestUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminCourseRequestState())
    val state: StateFlow<AdminCourseRequestState> = _state.asStateFlow()

    private val _effect = Channel<AdminCourseRequestEffect>()
    val effect: Flow<AdminCourseRequestEffect> = _effect.receiveAsFlow()

    fun load() {
        _state.update { it.copy(requests = AppResult.Loading) }
        viewModelScope.launch {
            _state.update { it.copy(requests = getAdminCourseRequestsUseCase()) }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            when (val r = deleteCourseRequestUseCase(id)) {
                is AppResult.Success -> {
                    _effect.send(AdminCourseRequestEffect.ShowSuccess("درخواست حذف شد."))
                    load()
                }
                is AppResult.Error -> _effect.send(AdminCourseRequestEffect.ShowError(r.message))
                else -> {}
            }
        }
    }
}
