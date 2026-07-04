package com.kazemieh.academy.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.academy.GetMyProjectUseCase
import com.kazemieh.domain.academy.ProjectSubmission
import com.kazemieh.domain.academy.SubmitProjectByLinkUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProjectSubmissionState(
    val isLoading: Boolean = false,
    val submission: ProjectSubmission? = null,
    val submitting: Boolean = false
)

sealed interface ProjectSubmissionEffect {
    data class ShowError(val message: Any) : ProjectSubmissionEffect
    data class ShowSuccess(val message: Any) : ProjectSubmissionEffect
}

class ProjectSubmissionViewModel(
    private val getMyProjectUseCase: GetMyProjectUseCase,
    private val submitProjectByLinkUseCase: SubmitProjectByLinkUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectSubmissionState())
    val state: StateFlow<ProjectSubmissionState> = _state.asStateFlow()

    private val _effect = Channel<ProjectSubmissionEffect>()
    val effect: Flow<ProjectSubmissionEffect> = _effect.receiveAsFlow()

    private var courseId: Long = 0

    fun load(courseId: Long) {
        this.courseId = courseId
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val r = getMyProjectUseCase(courseId)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, submission = r.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(ProjectSubmissionEffect.ShowError(r.message))
                }
                else -> {}
            }
        }
    }

    fun submit(fileUrl: String, note: String?) {
        _state.update { it.copy(submitting = true) }
        viewModelScope.launch {
            when (val r = submitProjectByLinkUseCase(courseId, fileUrl, note)) {
                is AppResult.Success -> {
                    _state.update { it.copy(submitting = false, submission = r.data) }
                    _effect.send(ProjectSubmissionEffect.ShowSuccess("پروژه ثبت شد و در انتظارِ بررسیِ مدرس است."))
                }
                is AppResult.Error -> {
                    _state.update { it.copy(submitting = false) }
                    _effect.send(ProjectSubmissionEffect.ShowError(r.message))
                }
                else -> {}
            }
        }
    }
}
