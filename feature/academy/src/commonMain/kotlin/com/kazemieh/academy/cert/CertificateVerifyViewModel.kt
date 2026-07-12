package com.kazemieh.academy.cert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.academy.CertificateVerification
import com.kazemieh.domain.academy.VerifyCertificateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CertificateVerifyState(
    val isChecking: Boolean = false,
    val result: CertificateVerification? = null,
    val searched: Boolean = false
)

/** تاییدِ عمومیِ گواهی — بدونِ نیازِ ورود، هر کسی می‌تواند با شماره‌ی گواهی، اصالتِ آن را بررسی کند. */
class CertificateVerifyViewModel(
    private val verifyCertificateUseCase: VerifyCertificateUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CertificateVerifyState())
    val state: StateFlow<CertificateVerifyState> = _state.asStateFlow()

    fun verify(certNumber: String) {
        if (certNumber.isBlank()) return
        _state.update { it.copy(isChecking = true, searched = true) }
        viewModelScope.launch {
            when (val result = verifyCertificateUseCase(certNumber.trim())) {
                is AppResult.Success -> _state.update { it.copy(isChecking = false, result = result.data) }
                is AppResult.Error -> _state.update { it.copy(isChecking = false, result = CertificateVerification(false, null, null, null)) }
                else -> {}
            }
        }
    }
}
