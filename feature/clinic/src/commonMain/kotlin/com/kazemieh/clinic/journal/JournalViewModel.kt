package com.kazemieh.clinic.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.clinic.AddJournalEntryUseCase
import com.kazemieh.domain.clinic.DeleteJournalEntryUseCase
import com.kazemieh.domain.clinic.GetMyJournalUseCase
import com.kazemieh.domain.clinic.JournalEntry
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JournalState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val entries: List<JournalEntry> = emptyList()
)

sealed interface JournalEffect {
    data class ShowError(val message: Any) : JournalEffect
}

class JournalViewModel(
    private val getMyJournalUseCase: GetMyJournalUseCase,
    private val addJournalEntryUseCase: AddJournalEntryUseCase,
    private val deleteJournalEntryUseCase: DeleteJournalEntryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(JournalState())
    val state: StateFlow<JournalState> = _state.asStateFlow()

    private val _effect = Channel<JournalEffect>()
    val effect: Flow<JournalEffect> = _effect.receiveAsFlow()

    fun load() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = getMyJournalUseCase()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, entries = result.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(JournalEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    fun add(content: String, sharedWithTherapistId: Long?) {
        if (content.isBlank() || _state.value.isSaving) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            when (val result = addJournalEntryUseCase(content.trim(), sharedWithTherapistId)) {
                is AppResult.Success -> {
                    _state.update { it.copy(isSaving = false, entries = listOf(result.data) + it.entries) }
                }
                is AppResult.Error -> {
                    _state.update { it.copy(isSaving = false) }
                    _effect.send(JournalEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            when (val result = deleteJournalEntryUseCase(id)) {
                is AppResult.Success -> _state.update { it.copy(entries = it.entries.filter { e -> e.id != id }) }
                is AppResult.Error -> _effect.send(JournalEffect.ShowError(result.message))
                else -> {}
            }
        }
    }
}
