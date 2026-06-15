package com.kazemieh.admin.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.story.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminStoryViewModel(
    private val getAdminStoriesUseCase: GetAdminStoriesUseCase,
    private val createStoryUseCase: CreateStoryUseCase,
    private val updateStoryUseCase: UpdateStoryUseCase,
    private val deleteStoryUseCase: DeleteStoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminStoryState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AdminStoryEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadStories()
    }

    fun handleIntent(intent: AdminStoryIntent) {
        when (intent) {
            is AdminStoryIntent.Refresh -> loadStories()
            is AdminStoryIntent.CreateStory -> createStory(intent.bytes, intent.mediaType, intent.productId, intent.title)
            is AdminStoryIntent.UpdateStory -> updateStory(intent.id, intent.productId, intent.title)
            is AdminStoryIntent.DeleteStory -> deleteStory(intent.id)
        }
    }

    private fun loadStories() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = getAdminStoriesUseCase()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, stories = result.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(AdminStoryEffect.ShowError(result.message))
                }
                is AppResult.Loading -> {}
            }
        }
    }

    private fun createStory(bytes: ByteArray, mediaType: StoryMediaType, productId: Long?, title: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            when (val result = createStoryUseCase(bytes, mediaType, productId, title)) {
                is AppResult.Success -> {
                    _effect.send(AdminStoryEffect.ShowSuccess("Story created successfully"))
                    loadStories()
                }
                is AppResult.Error -> _effect.send(AdminStoryEffect.ShowError(result.message))
                is AppResult.Loading -> {}
            }
            _state.update { it.copy(isSaving = false) }
        }
    }

    private fun updateStory(id: Long, productId: Long?, title: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            when (val result = updateStoryUseCase(id, productId, title)) {
                is AppResult.Success -> {
                    _effect.send(AdminStoryEffect.ShowSuccess("Story updated successfully"))
                    loadStories()
                }
                is AppResult.Error -> _effect.send(AdminStoryEffect.ShowError(result.message))
                is AppResult.Loading -> {}
            }
            _state.update { it.copy(isSaving = false) }
        }
    }

    private fun deleteStory(id: Long) {
        viewModelScope.launch {
            when (val result = deleteStoryUseCase(id)) {
                is AppResult.Success -> {
                    _effect.send(AdminStoryEffect.ShowSuccess("Story deleted successfully"))
                    loadStories()
                }
                is AppResult.Error -> _effect.send(AdminStoryEffect.ShowError(result.message))
                is AppResult.Loading -> {}
            }
        }
    }
}

data class AdminStoryState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val stories: List<Story> = emptyList()
)

sealed interface AdminStoryIntent {
    data object Refresh : AdminStoryIntent
    data class CreateStory(val bytes: ByteArray, val mediaType: StoryMediaType, val productId: Long?, val title: String?) : AdminStoryIntent
    data class UpdateStory(val id: Long, val productId: Long?, val title: String?) : AdminStoryIntent
    data class DeleteStory(val id: Long) : AdminStoryIntent
}

sealed interface AdminStoryEffect {
    data class ShowError(val message: Any) : AdminStoryEffect
    data class ShowSuccess(val message: Any) : AdminStoryEffect
}
