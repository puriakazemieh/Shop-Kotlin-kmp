package com.kazemieh.admin.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.admin.GetAdminQuestionsUseCase
import com.kazemieh.domain.admin.GetAdminReviewsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminInteractionsViewModel(
    private val getAdminReviewsUseCase: GetAdminReviewsUseCase,
    private val getAdminQuestionsUseCase: GetAdminQuestionsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminInteractionsState())
    val state: StateFlow<AdminInteractionsState> = _state.asStateFlow()

    init {
        handleIntent(AdminInteractionsIntent.LoadInteractions)
    }

    fun handleIntent(intent: AdminInteractionsIntent) {
        when (intent) {
            is AdminInteractionsIntent.LoadInteractions -> loadInteractions()
            is AdminInteractionsIntent.SelectTab -> _state.update { it.copy(selectedTab = intent.index) }
            is AdminInteractionsIntent.SetNewFilter -> {
                _state.update { it.copy(isNewFilter = intent.isNew) }
                loadInteractions()
            }
            is AdminInteractionsIntent.SetProductFilter -> {
                _state.update { it.copy(productIdFilter = intent.productId) }
                loadInteractions()
            }
        }
    }

    private fun loadInteractions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val currentFilter = _state.value
            
            val reviewsResult = getAdminReviewsUseCase(
                productId = currentFilter.productIdFilter,
                isNew = currentFilter.isNewFilter
            )
            
            val questionsResult = getAdminQuestionsUseCase(
                productId = currentFilter.productIdFilter,
                isNew = currentFilter.isNewFilter
            )

            if (reviewsResult is AppResult.Success && questionsResult is AppResult.Success) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        reviews = reviewsResult.data.items,
                        questions = questionsResult.data.items,
                        error = null
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load interactions"
                    )
                }
            }
        }
    }
}
