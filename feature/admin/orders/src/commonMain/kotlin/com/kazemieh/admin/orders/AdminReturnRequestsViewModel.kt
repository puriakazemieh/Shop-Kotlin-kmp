package com.kazemieh.admin.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.order.AdminListReturnRequestsUseCase
import com.kazemieh.domain.order.AdminReturnRequest
import com.kazemieh.domain.order.AdminUpdateReturnRequestUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminReturnRequestsState(
    val isLoading: Boolean = false,
    val requests: List<AdminReturnRequest> = emptyList()
)

sealed interface AdminReturnRequestsEffect {
    data class ShowError(val message: Any) : AdminReturnRequestsEffect
    data class ShowSuccess(val message: Any) : AdminReturnRequestsEffect
}

class AdminReturnRequestsViewModel(
    private val adminListReturnRequestsUseCase: AdminListReturnRequestsUseCase,
    private val adminUpdateReturnRequestUseCase: AdminUpdateReturnRequestUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminReturnRequestsState())
    val state: StateFlow<AdminReturnRequestsState> = _state.asStateFlow()

    private val _effect = Channel<AdminReturnRequestsEffect>()
    val effect: Flow<AdminReturnRequestsEffect> = _effect.receiveAsFlow()

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = adminListReturnRequestsUseCase()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, requests = result.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(AdminReturnRequestsEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    fun updateStatus(id: Long, status: String, adminNote: String?) {
        viewModelScope.launch {
            when (val result = adminUpdateReturnRequestUseCase(id, status, adminNote)) {
                is AppResult.Success -> {
                    _effect.send(AdminReturnRequestsEffect.ShowSuccess("وضعیت به‌روزرسانی شد."))
                    load()
                }
                is AppResult.Error -> _effect.send(AdminReturnRequestsEffect.ShowError(result.message))
                else -> {}
            }
        }
    }
}
