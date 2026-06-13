package com.kazemieh.admin.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AdminDiscountsViewModel(
    private val getAdminDiscountsUseCase: GetAdminDiscountsUseCase,
    private val createDiscountUseCase: AdminCreateDiscountUseCase,
    private val updateAdminDiscountUseCase: UpdateAdminDiscountUseCase,
    private val deleteAdminDiscountUseCase: DeleteAdminDiscountUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminDiscountsState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AdminDiscountsEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadDiscounts()
    }

    fun handleIntent(intent: AdminDiscountsIntent) {
        when (intent) {
            is AdminDiscountsIntent.Refresh -> loadDiscounts()
            is AdminDiscountsIntent.CreateDiscount -> createDiscount(intent.param)
            is AdminDiscountsIntent.UpdateDiscount -> updateDiscount(intent.id, intent.param)
            is AdminDiscountsIntent.DeleteDiscount -> deleteDiscount(intent.id)
            is AdminDiscountsIntent.Search -> {
                _state.update { it.copy(searchQuery = intent.query) }
            }
        }
    }

    private fun loadDiscounts() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            val result = getAdminDiscountsUseCase()
            _state.update { it.copy(discountsState = result, isRefreshing = false) }
        }
    }

    private fun createDiscount(param: CreateDiscountParam) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val result = createDiscountUseCase(param)) {
                is AppResult.Success -> {
                    _effect.send(AdminDiscountsEffect.Success("Discount created successfully"))
                    loadDiscounts()
                }
                is AppResult.Error -> {
                    _effect.send(AdminDiscountsEffect.ShowError(result.message))
                }
                else -> {}
            }
            _state.update { it.copy(isActionLoading = false) }
        }
    }

    private fun updateDiscount(id: Long, param: UpdateDiscountParam) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val result = updateAdminDiscountUseCase(id, param)) {
                is AppResult.Success -> {
                    _effect.send(AdminDiscountsEffect.Success("Discount updated successfully"))
                    loadDiscounts()
                }
                is AppResult.Error -> {
                    _effect.send(AdminDiscountsEffect.ShowError(result.message))
                }
                else -> {}
            }
            _state.update { it.copy(isActionLoading = false) }
        }
    }

    private fun deleteDiscount(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            when (val result = deleteAdminDiscountUseCase(id)) {
                is AppResult.Success -> {
                    _effect.send(AdminDiscountsEffect.Success("Discount deleted successfully"))
                    loadDiscounts()
                }
                is AppResult.Error -> {
                    _effect.send(AdminDiscountsEffect.ShowError(result.message))
                }
                else -> {}
            }
            _state.update { it.copy(isActionLoading = false) }
        }
    }
}

data class AdminDiscountsState(
    val discountsState: AppResult<List<Discount>> = AppResult.Loading,
    val searchQuery: String = "",
    val isRefreshing: Boolean = false,
    val isActionLoading: Boolean = false
) {
    val filteredDiscounts: List<Discount>
        get() = (discountsState as? AppResult.Success)?.data?.filter {
            it.code.contains(searchQuery, ignoreCase = true)
        } ?: emptyList()
}

sealed interface AdminDiscountsIntent {
    data object Refresh : AdminDiscountsIntent
    data class CreateDiscount(val param: CreateDiscountParam) : AdminDiscountsIntent
    data class UpdateDiscount(val id: Long, val param: UpdateDiscountParam) : AdminDiscountsIntent
    data class DeleteDiscount(val id: Long) : AdminDiscountsIntent
    data class Search(val query: String) : AdminDiscountsIntent
}

sealed interface AdminDiscountsEffect {
    data class ShowError(val message: Any) : AdminDiscountsEffect
    data class Success(val message: String) : AdminDiscountsEffect
}
