package com.kazemieh.academy.courserequest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.courserequest.CourseRequest
import com.kazemieh.domain.courserequest.CreateCourseRequestUseCase
import com.kazemieh.domain.courserequest.GetCourseRequestsUseCase
import com.kazemieh.domain.courserequest.ToggleCourseRequestLikeUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CourseRequestState(
    val requests: AppResult<List<CourseRequest>> = AppResult.Loading,
    val submitting: Boolean = false
)

sealed interface CourseRequestEffect {
    data class ShowError(val message: Any) : CourseRequestEffect
    data object Submitted : CourseRequestEffect
}

class CourseRequestViewModel(
    private val getCourseRequestsUseCase: GetCourseRequestsUseCase,
    private val createCourseRequestUseCase: CreateCourseRequestUseCase,
    private val toggleLikeUseCase: ToggleCourseRequestLikeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CourseRequestState())
    val state: StateFlow<CourseRequestState> = _state.asStateFlow()

    private val _effect = Channel<CourseRequestEffect>()
    val effect: Flow<CourseRequestEffect> = _effect.receiveAsFlow()

    init {
        load()
    }

    fun load() {
        _state.update { it.copy(requests = AppResult.Loading) }
        viewModelScope.launch {
            _state.update { it.copy(requests = getCourseRequestsUseCase()) }
        }
    }

    fun submit(title: String, description: String?) {
        if (title.isBlank()) return
        _state.update { it.copy(submitting = true) }
        viewModelScope.launch {
            when (val r = createCourseRequestUseCase(title.trim(), description?.trim()?.ifBlank { null })) {
                is AppResult.Success -> {
                    _state.update { it.copy(submitting = false) }
                    _effect.send(CourseRequestEffect.Submitted)
                    load()
                }
                is AppResult.Error -> {
                    _state.update { it.copy(submitting = false) }
                    _effect.send(CourseRequestEffect.ShowError(r.message))
                }
                else -> _state.update { it.copy(submitting = false) }
            }
        }
    }

    fun toggleLike(request: CourseRequest) {
        // به‌روزرسانیِ خوش‌بینانه
        updateItem(request.id) {
            val nowLiked = !it.liked
            it.copy(liked = nowLiked, likeCount = (it.likeCount + if (nowLiked) 1 else -1).coerceAtLeast(0))
        }
        viewModelScope.launch {
            when (val r = toggleLikeUseCase(request.id)) {
                is AppResult.Success -> updateItem(request.id) {
                    it.copy(liked = r.data.first, likeCount = r.data.second)
                }
                is AppResult.Error -> {
                    // بازگردانی در صورتِ خطا
                    updateItem(request.id) {
                        it.copy(liked = request.liked, likeCount = request.likeCount)
                    }
                    _effect.send(CourseRequestEffect.ShowError(r.message))
                }
                else -> {}
            }
        }
    }

    private fun updateItem(id: Long, transform: (CourseRequest) -> CourseRequest) {
        val current = _state.value.requests
        if (current is AppResult.Success) {
            _state.update {
                it.copy(requests = AppResult.Success(current.data.map { r -> if (r.id == id) transform(r) else r }))
            }
        }
    }
}
