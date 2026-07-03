package com.kazemieh.clinic.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.clinic.GetTherapistsUseCase
import com.kazemieh.domain.clinic.TherapistSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TherapistListState(
    val isLoading: Boolean = false,
    val therapists: List<TherapistSummary> = emptyList(),
    val error: Any? = null
)

class TherapistListViewModel(
    private val getTherapistsUseCase: GetTherapistsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TherapistListState())
    val state: StateFlow<TherapistListState> = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = getTherapistsUseCase()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, therapists = result.data, error = null) }
                is AppResult.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                else -> {}
            }
        }
    }
}
