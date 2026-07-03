package com.kazemieh.admin.academy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.academy.AddCourseLessonUseCase
import com.kazemieh.domain.academy.AddCourseSectionUseCase
import com.kazemieh.domain.academy.AdminCourseParams
import com.kazemieh.domain.academy.CourseDetail
import com.kazemieh.domain.academy.CourseSummary
import com.kazemieh.domain.academy.CreateCourseUseCase
import com.kazemieh.domain.academy.DeleteCourseUseCase
import com.kazemieh.domain.academy.GetAdminCourseDetailUseCase
import com.kazemieh.domain.academy.GetAdminCoursesUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminAcademyState(
    val isLoading: Boolean = false,
    val courses: List<CourseSummary> = emptyList(),
    val expandedCourseId: Long? = null,
    val expandedCourseDetail: CourseDetail? = null,
    val loadingDetail: Boolean = false
)

sealed interface AdminAcademyEffect {
    data class ShowError(val message: Any) : AdminAcademyEffect
    data class ShowSuccess(val message: Any) : AdminAcademyEffect
}

class AdminAcademyViewModel(
    private val getAdminCoursesUseCase: GetAdminCoursesUseCase,
    private val getAdminCourseDetailUseCase: GetAdminCourseDetailUseCase,
    private val createCourseUseCase: CreateCourseUseCase,
    private val deleteCourseUseCase: DeleteCourseUseCase,
    private val addCourseSectionUseCase: AddCourseSectionUseCase,
    private val addCourseLessonUseCase: AddCourseLessonUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminAcademyState())
    val state: StateFlow<AdminAcademyState> = _state.asStateFlow()

    private val _effect = Channel<AdminAcademyEffect>()
    val effect: Flow<AdminAcademyEffect> = _effect.receiveAsFlow()

    fun load() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = getAdminCoursesUseCase()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, courses = result.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(AdminAcademyEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    fun toggleExpand(courseId: Long) {
        if (_state.value.expandedCourseId == courseId) {
            _state.update { it.copy(expandedCourseId = null, expandedCourseDetail = null) }
            return
        }
        _state.update { it.copy(expandedCourseId = courseId, expandedCourseDetail = null, loadingDetail = true) }
        viewModelScope.launch {
            when (val result = getAdminCourseDetailUseCase(courseId)) {
                is AppResult.Success -> _state.update { it.copy(loadingDetail = false, expandedCourseDetail = result.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(loadingDetail = false) }
                    _effect.send(AdminAcademyEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    fun createCourse(title: String, slug: String, price: String, productId: String) {
        viewModelScope.launch {
            val params = AdminCourseParams(
                title = title,
                slug = slug,
                price = price.toDoubleOrNull() ?: 0.0,
                productId = productId.toLongOrNull()
            )
            when (val result = createCourseUseCase(params)) {
                is AppResult.Success -> {
                    _effect.send(AdminAcademyEffect.ShowSuccess("دوره ساخته شد."))
                    load()
                }
                is AppResult.Error -> _effect.send(AdminAcademyEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    fun deleteCourse(id: Long) {
        viewModelScope.launch {
            when (val result = deleteCourseUseCase(id)) {
                is AppResult.Success -> {
                    _effect.send(AdminAcademyEffect.ShowSuccess("دوره حذف شد."))
                    if (_state.value.expandedCourseId == id) {
                        _state.update { it.copy(expandedCourseId = null, expandedCourseDetail = null) }
                    }
                    load()
                }
                is AppResult.Error -> _effect.send(AdminAcademyEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    fun addSection(courseId: Long, title: String) {
        viewModelScope.launch {
            when (val result = addCourseSectionUseCase(courseId, title)) {
                is AppResult.Success -> {
                    _effect.send(AdminAcademyEffect.ShowSuccess("بخش اضافه شد."))
                    refreshExpanded(courseId)
                }
                is AppResult.Error -> _effect.send(AdminAcademyEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    fun addLesson(courseId: Long, sectionId: Long, title: String, videoUrl: String, durationMinutes: String, isFreePreview: Boolean) {
        viewModelScope.launch {
            val durationSeconds = (durationMinutes.toDoubleOrNull() ?: 0.0) * 60
            when (val result = addCourseLessonUseCase(
                courseId, sectionId, title,
                videoUrl.ifBlank { null }, durationSeconds.toInt(), 0, isFreePreview
            )) {
                is AppResult.Success -> {
                    _effect.send(AdminAcademyEffect.ShowSuccess("درس اضافه شد."))
                    refreshExpanded(courseId)
                }
                is AppResult.Error -> _effect.send(AdminAcademyEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    private fun refreshExpanded(courseId: Long) {
        viewModelScope.launch {
            when (val result = getAdminCourseDetailUseCase(courseId)) {
                is AppResult.Success -> _state.update { it.copy(expandedCourseDetail = result.data) }
                else -> {}
            }
        }
        load()
    }
}
