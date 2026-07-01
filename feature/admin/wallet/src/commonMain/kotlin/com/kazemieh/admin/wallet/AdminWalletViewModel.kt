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

data class AdminWalletState(
    val usersState: AppResult<List<AdminWalletUser>> = AppResult.Success(emptyList()),
    val searchQuery: String = "",
    val isAdjusting: Boolean = false
)

sealed interface AdminWalletIntent {
    data class Search(val query: String) : AdminWalletIntent
    data class AdjustBalance(val userId: Long, val amount: Double, val description: String?) : AdminWalletIntent
}

sealed interface AdminWalletEffect {
    data class ShowError(val message: Any) : AdminWalletEffect
    data object AdjustSuccess : AdminWalletEffect
}

class AdminWalletViewModel(
    private val searchWalletUsersUseCase: AdminSearchWalletUsersUseCase,
    private val adjustWalletUseCase: AdminAdjustWalletUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminWalletState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AdminWalletEffect>()
    val effect = _effect.asSharedFlow()

    fun handleIntent(intent: AdminWalletIntent) {
        when (intent) {
            is AdminWalletIntent.Search -> searchUsers(intent.query)
            is AdminWalletIntent.AdjustBalance -> adjustBalance(intent.userId, intent.amount, intent.description)
        }
    }

    private fun searchUsers(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.length < 2) {
            _state.update { it.copy(usersState = AppResult.Success(emptyList())) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(usersState = AppResult.Loading) }
            val result = searchWalletUsersUseCase(query)
            _state.update { it.copy(usersState = result) }
        }
    }

    private fun adjustBalance(userId: Long, amount: Double, description: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isAdjusting = true) }
            val result = adjustWalletUseCase(userId, amount, description)
            _state.update { it.copy(isAdjusting = false) }

            when (result) {
                is AppResult.Success -> {
                    _effect.emit(AdminWalletEffect.AdjustSuccess)
                    searchUsers(_state.value.searchQuery)
                }
                is AppResult.Error -> {
                    _effect.emit(AdminWalletEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }
}
