package com.kazemieh.academy.cert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.academy.Certificate
import com.kazemieh.domain.academy.GetCertificatesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CertificatesState(
    val isLoading: Boolean = false,
    val certificates: List<Certificate> = emptyList(),
    val error: Any? = null
)

class CertificatesViewModel(
    private val getCertificatesUseCase: GetCertificatesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CertificatesState())
    val state: StateFlow<CertificatesState> = _state.asStateFlow()

    fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val r = getCertificatesUseCase()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, certificates = r.data) }
                is AppResult.Error -> _state.update { it.copy(isLoading = false, error = r.message) }
                else -> {}
            }
        }
    }
}
