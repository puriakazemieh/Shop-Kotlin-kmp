package com.kazemieh.catalog.bundle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.bundle.BundleSummary
import com.kazemieh.domain.bundle.GetBundlesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BundleListState(
    val isLoading: Boolean = false,
    val bundles: List<BundleSummary> = emptyList(),
    val error: Any? = null
)

class BundleListViewModel(
    private val getBundlesUseCase: GetBundlesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BundleListState())
    val state: StateFlow<BundleListState> = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = getBundlesUseCase()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, bundles = result.data) }
                is AppResult.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                else -> {}
            }
        }
    }
}
