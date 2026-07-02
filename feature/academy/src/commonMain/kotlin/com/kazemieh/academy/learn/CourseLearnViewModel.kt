package com.kazemieh.academy.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.academy.CourseDetail
import com.kazemieh.domain.academy.GetCourseDetailUseCase
import com.kazemieh.domain.academy.Lesson
import com.kazemieh.domain.academy.UpdateLessonProgressUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CourseLearnState(
    val isLoading: Boolean = false,
    val course: CourseDetail? = null,
    val selectedLessonId: Long? = null,
    val error: Any? = null
) {
    val allLessons: List<Lesson> get() = course?.sections?.flatMap { it.lessons } ?: emptyList()
    val selectedLesson: Lesson? get() = allLessons.firstOrNull { it.id == selectedLessonId }
}

class CourseLearnViewModel(
    private val getCourseDetailUseCase: GetCourseDetailUseCase,
    private val updateLessonProgressUseCase: UpdateLessonProgressUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CourseLearnState())
    val state: StateFlow<CourseLearnState> = _state.asStateFlow()

    fun load(slug: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = getCourseDetailUseCase(slug)) {
                is AppResult.Success -> {
                    val course = result.data
                    val current = _state.value.selectedLessonId
                    // اولین درسِ قابلِ تماشا را انتخاب کن اگر چیزی انتخاب نشده
                    val firstPlayable = course.sections.flatMap { it.lessons }
                        .firstOrNull { it.videoUrl != null }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            course = course,
                            selectedLessonId = current ?: firstPlayable?.id,
                            error = null
                        )
                    }
                }
                is AppResult.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                else -> {}
            }
        }
    }

    fun selectLesson(lessonId: Long) {
        _state.update { it.copy(selectedLessonId = lessonId) }
    }

    fun markComplete(lessonId: Long) {
        val slug = _state.value.course?.slug ?: return
        viewModelScope.launch {
            when (updateLessonProgressUseCase(lessonId, completed = true)) {
                is AppResult.Success -> load(slug) // رفرش برای به‌روزرسانیِ تیک‌ها و درصد
                else -> {}
            }
        }
    }
}
