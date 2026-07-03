package com.kazemieh.psychtest.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.psychtest.GetMyPsychTestsUseCase
import com.kazemieh.domain.psychtest.GetPsychTestsUseCase
import com.kazemieh.domain.psychtest.PsychTestSummary
import com.kazemieh.domain.psychtest.UserPsychTest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PsychTestListState(
    val isLoading: Boolean = false,
    val tests: List<PsychTestSummary> = emptyList(),
    val myTests: List<UserPsychTest> = emptyList(),
    val error: Any? = null
)

class PsychTestListViewModel(
    private val getPsychTestsUseCase: GetPsychTestsUseCase,
    private val getMyPsychTestsUseCase: GetMyPsychTestsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PsychTestListState())
    val state: StateFlow<PsychTestListState> = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val r = getPsychTestsUseCase()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, tests = r.data) }
                is AppResult.Error -> _state.update { it.copy(isLoading = false, error = r.message) }
                else -> {}
            }
        }
        loadMyTests()
    }

    fun loadMyTests() {
        viewModelScope.launch {
            when (val r = getMyPsychTestsUseCase()) {
                is AppResult.Success -> _state.update { it.copy(myTests = r.data) }
                else -> {}
            }
        }
    }
}
