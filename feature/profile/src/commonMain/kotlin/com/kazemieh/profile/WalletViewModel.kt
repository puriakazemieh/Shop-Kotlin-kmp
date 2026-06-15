package com.kazemieh.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.AdminPage
import com.kazemieh.domain.wallet.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WalletState(
    val balanceState: AppResult<WalletBalance> = AppResult.Loading,
    val transactionsState: AppResult<AdminPage<WalletTransaction>> = AppResult.Loading,
    val isTopUpLoading: Boolean = false,
    val isWithdrawLoading: Boolean = false,
    val topUpUrl: String? = null
)

sealed interface WalletIntent {
    data object LoadWallet : WalletIntent
    data class TopUp(val amount: Double) : WalletIntent
    data class Withdraw(val amount: Double, val iban: String) : WalletIntent
    data object ClearTopUpUrl : WalletIntent
}

sealed interface WalletEffect {
    data class ShowError(val message: String) : WalletEffect
    data class ShowSuccess(val message: String) : WalletEffect
    data class NavigateToPayment(val url: String) : WalletEffect
}

class WalletViewModel(
    private val getWalletBalanceUseCase: GetWalletBalanceUseCase,
    private val getWalletTransactionsUseCase: GetWalletTransactionsUseCase,
    private val topUpWalletUseCase: TopUpWalletUseCase,
    private val withdrawWalletUseCase: WithdrawWalletUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(WalletState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<WalletEffect>()
    val effect = _effect.asSharedFlow()

    init {
        handleIntent(WalletIntent.LoadWallet)
    }

    fun handleIntent(intent: WalletIntent) {
        when (intent) {
            is WalletIntent.LoadWallet -> loadWallet()
            is WalletIntent.TopUp -> topUp(intent.amount)
            is WalletIntent.Withdraw -> withdraw(intent.amount, intent.iban)
            is WalletIntent.ClearTopUpUrl -> _state.update { it.copy(topUpUrl = null) }
        }
    }

    private fun loadWallet() {
        viewModelScope.launch {
            _state.update { it.copy(balanceState = AppResult.Loading, transactionsState = AppResult.Loading) }
            
            val balanceResult = getWalletBalanceUseCase()
            _state.update { it.copy(balanceState = balanceResult) }

            val transactionsResult = getWalletTransactionsUseCase(0, 50)
            _state.update { it.copy(transactionsState = transactionsResult) }
        }
    }

    private fun topUp(amount: Double) {
        viewModelScope.launch {
            _state.update { it.copy(isTopUpLoading = true) }
            val result = topUpWalletUseCase(amount)
            _state.update { it.copy(isTopUpLoading = false) }
            
            when (result) {
                is AppResult.Success -> {
                    _effect.emit(WalletEffect.NavigateToPayment(result.data))
                }
                is AppResult.Error -> {
                    _effect.emit(WalletEffect.ShowError(result.message.toString()))
                }
                else -> {}
            }
        }
    }

    private fun withdraw(amount: Double, iban: String) {
        viewModelScope.launch {
            _state.update { it.copy(isWithdrawLoading = true) }
            val result = withdrawWalletUseCase(amount, iban)
            _state.update { it.copy(isWithdrawLoading = false) }

            when (result) {
                is AppResult.Success -> {
                    _effect.emit(WalletEffect.ShowSuccess("درخواست برداشت با موفقیت ثبت شد"))
                    loadWallet()
                }
                is AppResult.Error -> {
                    _effect.emit(WalletEffect.ShowError(result.message.toString()))
                }
                else -> {}
            }
        }
    }
}
