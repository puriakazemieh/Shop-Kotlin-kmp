package com.kazemieh.comparison

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.common.ComparisonStore
import com.kazemieh.domain.catalog.GetProductDetailUseCase
import com.kazemieh.domain.catalog.ProductDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ComparisonState(
    val isLoading: Boolean = false,
    val products: List<ProductDetail> = emptyList()
)

class ComparisonViewModel(
    private val getProductDetailUseCase: GetProductDetailUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ComparisonState())
    val state: StateFlow<ComparisonState> = _state.asStateFlow()

    init {
        // با هر تغییرِ فهرستِ مقایسه، جزئیاتِ محصولات را دوباره می‌گیریم.
        viewModelScope.launch {
            ComparisonStore.slugs.collect { slugs -> loadDetails(slugs) }
        }
    }

    private suspend fun loadDetails(slugs: List<String>) {
        if (slugs.isEmpty()) {
            _state.update { it.copy(isLoading = false, products = emptyList()) }
            return
        }
        _state.update { it.copy(isLoading = true) }
        val loaded = mutableListOf<ProductDetail>()
        for (slug in slugs) {
            when (val r = getProductDetailUseCase(slug)) {
                is AppResult.Success -> loaded.add(r.data)
                else -> {}
            }
        }
        _state.update { it.copy(isLoading = false, products = loaded) }
    }

    fun remove(slug: String) = ComparisonStore.remove(slug)
    fun clear() = ComparisonStore.clear()
}
