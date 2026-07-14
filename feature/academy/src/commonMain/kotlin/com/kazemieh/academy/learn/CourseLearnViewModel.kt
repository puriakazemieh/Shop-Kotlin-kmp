package com.kazemieh.academy.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.academy.CourseDetail
import com.kazemieh.domain.academy.CreateLessonQuestionUseCase
import com.kazemieh.domain.academy.GetCourseDetailUseCase
import com.kazemieh.domain.academy.GetLessonQuestionsUseCase
import com.kazemieh.domain.academy.Lesson
import com.kazemieh.domain.academy.LessonQuestion
import com.kazemieh.domain.academy.MarkCourseUpdateSeenUseCase
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
    val error: Any? = null,
    /** پرسش‌وپاسخِ هر درس (mini-forum زیرِ ویدیو)، per lessonId. */
    val questionsByLesson: Map<Long, List<LessonQuestion>> = emptyMap(),
    val loadingQuestions: Boolean = false
) {
    val allLessons: List<Lesson> get() = course?.sections?.flatMap { it.lessons } ?: emptyList()
    val selectedLesson: Lesson? get() = allLessons.firstOrNull { it.id == selectedLessonId }
}

class CourseLearnViewModel(
    private val getCourseDetailUseCase: GetCourseDetailUseCase,
    private val updateLessonProgressUseCase: UpdateLessonProgressUseCase,
    private val markCourseUpdateSeenUseCase: MarkCourseUpdateSeenUseCase,
    private val getLessonQuestionsUseCase: GetLessonQuestionsUseCase,
    private val createLessonQuestionUseCase: CreateLessonQuestionUseCase
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
                    // با بازکردنِ صفحه، نشانِ «به‌روزرسانیِ جدید» پاک می‌شود
                    if (course.hasUnseenUpdate) {
                        viewModelScope.launch { markCourseUpdateSeenUseCase(course.id) }
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

    /** پرسش‌وپاسخِ درس — mini-forum زیرِ ویدیو. */
    fun loadQuestions(lessonId: Long) {
        _state.update { it.copy(loadingQuestions = true) }
        viewModelScope.launch {
            when (val result = getLessonQuestionsUseCase(lessonId)) {
                is AppResult.Success -> _state.update {
                    it.copy(loadingQuestions = false, questionsByLesson = it.questionsByLesson + (lessonId to result.data))
                }
                else -> _state.update { it.copy(loadingQuestions = false) }
            }
        }
    }

    fun submitQuestion(lessonId: Long, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            when (createLessonQuestionUseCase(lessonId, content)) {
                is AppResult.Success -> loadQuestions(lessonId)
                else -> {}
            }
        }
    }
}
