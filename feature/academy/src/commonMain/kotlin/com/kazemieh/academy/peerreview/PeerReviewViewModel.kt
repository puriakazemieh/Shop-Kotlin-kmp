package com.kazemieh.academy.peerreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.academy.AddPeerCommentUseCase
import com.kazemieh.domain.academy.GetPeerCommentsUseCase
import com.kazemieh.domain.academy.GetPeerSubmissionsUseCase
import com.kazemieh.domain.academy.PeerComment
import com.kazemieh.domain.academy.ProjectSubmission
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PeerReviewState(
    val isLoading: Boolean = false,
    val submissions: List<ProjectSubmission> = emptyList(),
    val expandedSubmissionId: Long? = null,
    val commentsBySubmission: Map<Long, List<PeerComment>> = emptyMap(),
    val loadingComments: Boolean = false,
    val error: Any? = null
)

sealed interface PeerReviewEffect {
    data class ShowError(val message: Any) : PeerReviewEffect
}

class PeerReviewViewModel(
    private val getPeerSubmissionsUseCase: GetPeerSubmissionsUseCase,
    private val getPeerCommentsUseCase: GetPeerCommentsUseCase,
    private val addPeerCommentUseCase: AddPeerCommentUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PeerReviewState())
    val state: StateFlow<PeerReviewState> = _state.asStateFlow()

    private val _effect = Channel<PeerReviewEffect>()
    val effect: Flow<PeerReviewEffect> = _effect.receiveAsFlow()

    fun load(courseId: Long) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = getPeerSubmissionsUseCase(courseId)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, submissions = result.data, error = null) }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false, error = result.message) }
                    _effect.send(PeerReviewEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    fun toggleExpand(submissionId: Long) {
        val next = if (_state.value.expandedSubmissionId == submissionId) null else submissionId
        _state.update { it.copy(expandedSubmissionId = next) }
        if (next != null) loadComments(next)
    }

    private fun loadComments(submissionId: Long) {
        _state.update { it.copy(loadingComments = true) }
        viewModelScope.launch {
            when (val result = getPeerCommentsUseCase(submissionId)) {
                is AppResult.Success -> _state.update {
                    it.copy(loadingComments = false, commentsBySubmission = it.commentsBySubmission + (submissionId to result.data))
                }
                else -> _state.update { it.copy(loadingComments = false) }
            }
        }
    }

    fun addComment(submissionId: Long, comment: String) {
        if (comment.isBlank()) return
        viewModelScope.launch {
            when (val result = addPeerCommentUseCase(submissionId, comment)) {
                is AppResult.Success -> loadComments(submissionId)
                is AppResult.Error -> _effect.send(PeerReviewEffect.ShowError(result.message))
                else -> {}
            }
        }
    }
}
