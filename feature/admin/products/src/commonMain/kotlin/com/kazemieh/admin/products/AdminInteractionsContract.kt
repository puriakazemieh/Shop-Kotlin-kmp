package com.kazemieh.admin.products

import com.kazemieh.domain.admin.AdminInteraction

data class AdminInteractionsState(
    val isLoading: Boolean = false,
    val reviews: List<AdminInteraction> = emptyList(),
    val questions: List<AdminInteraction> = emptyList(),
    val error: Any? = null,
    val selectedTab: Int = 0, // 0: Reviews, 1: Questions
    val isNewFilter: Boolean? = null,
    val productIdFilter: Long? = null
)

sealed interface AdminInteractionsIntent {
    data object LoadInteractions : AdminInteractionsIntent
    data class SelectTab(val index: Int) : AdminInteractionsIntent
    data class SetNewFilter(val isNew: Boolean?) : AdminInteractionsIntent
    data class SetProductFilter(val productId: Long?) : AdminInteractionsIntent
}
