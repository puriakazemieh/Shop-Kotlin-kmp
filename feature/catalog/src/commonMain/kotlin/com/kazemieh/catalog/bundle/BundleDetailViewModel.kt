package com.kazemieh.catalog.bundle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.bundle.BundleDetail
import com.kazemieh.domain.bundle.GetBundleDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BundleDetailState(
    val isLoading: Boolean = false,
    val bundle: BundleDetail? = null,
    val error: Any? = null
)

class BundleDetailViewModel(
    private val getBundleDetailUseCase: GetBundleDetailUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BundleDetailState())
    val state: StateFlow<BundleDetailState> = _state.asStateFlow()

    fun load(slug: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = getBundleDetailUseCase(slug)) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, bundle = result.data) }
                is AppResult.Error -> _state.update { it.copy(isLoading = false, error = result.message) }
                else -> {}
            }
        }
    }
}
