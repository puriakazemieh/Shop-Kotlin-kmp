package com.kazemieh.admin.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.wallet.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminWithdrawalsState(
    val withdrawalsState: AppResult<List<AdminWithdrawal>> = AppResult.Loading,
    val isProcessing: Boolean = false
)

sealed interface AdminWithdrawalsIntent {
    data object LoadWithdrawals : AdminWithdrawalsIntent
    data class ProcessWithdrawal(val id: Long, val status: String, val adminNote: String?) : AdminWithdrawalsIntent
}

sealed interface AdminWithdrawalsEffect {
    data class ShowError(val message: String) : AdminWithdrawalsEffect
    data object ProcessSuccess : AdminWithdrawalsEffect
}

class AdminWithdrawalsViewModel(
    private val listWithdrawalsUseCase: AdminListWithdrawalsUseCase,
    private val processWithdrawalUseCase: AdminProcessWithdrawalUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminWithdrawalsState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AdminWithdrawalsEffect>()
    val effect = _effect.asSharedFlow()

    init {
        loadWithdrawals()
    }

    fun handleIntent(intent: AdminWithdrawalsIntent) {
        when (intent) {
            is AdminWithdrawalsIntent.LoadWithdrawals -> loadWithdrawals()
            is AdminWithdrawalsIntent.ProcessWithdrawal -> processWithdrawal(intent.id, intent.status, intent.adminNote)
        }
    }

    private fun loadWithdrawals() {
        viewModelScope.launch {
            _state.update { it.copy(withdrawalsState = AppResult.Loading) }
            val result = listWithdrawalsUseCase("PENDING")
            _state.update { it.copy(withdrawalsState = result) }
        }
    }

    private fun processWithdrawal(id: Long, status: String, adminNote: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true) }
            val result = processWithdrawalUseCase(id, status, adminNote)
            _state.update { it.copy(isProcessing = false) }

            when (result) {
                is AppResult.Success -> {
                    _effect.emit(AdminWithdrawalsEffect.ProcessSuccess)
                    loadWithdrawals()
                }
                is AppResult.Error -> {
                    _effect.emit(AdminWithdrawalsEffect.ShowError(result.message.toString()))
                }
                else -> {}
            }
        }
    }
}
