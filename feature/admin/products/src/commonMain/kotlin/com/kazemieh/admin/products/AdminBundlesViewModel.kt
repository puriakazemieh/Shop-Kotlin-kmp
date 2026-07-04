package com.kazemieh.admin.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.bundle.AdminBundle
import com.kazemieh.domain.bundle.AdminBundleParams
import com.kazemieh.domain.bundle.CreateBundleUseCase
import com.kazemieh.domain.bundle.DeleteBundleUseCase
import com.kazemieh.domain.bundle.GetAdminBundlesUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminBundlesState(
    val isLoading: Boolean = false,
    val bundles: List<AdminBundle> = emptyList()
)

sealed interface AdminBundlesEffect {
    data class ShowError(val message: Any) : AdminBundlesEffect
    data class ShowSuccess(val message: Any) : AdminBundlesEffect
}

class AdminBundlesViewModel(
    private val getAdminBundlesUseCase: GetAdminBundlesUseCase,
    private val createBundleUseCase: CreateBundleUseCase,
    private val deleteBundleUseCase: DeleteBundleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminBundlesState())
    val state: StateFlow<AdminBundlesState> = _state.asStateFlow()

    private val _effect = Channel<AdminBundlesEffect>()
    val effect: Flow<AdminBundlesEffect> = _effect.receiveAsFlow()

    fun load() {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = getAdminBundlesUseCase()) {
                is AppResult.Success -> _state.update { it.copy(isLoading = false, bundles = result.data) }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(AdminBundlesEffect.ShowError(result.message))
                }
                else -> {}
            }
        }
    }

    fun createBundle(title: String, slug: String, description: String, productId: String, memberProductIds: String) {
        viewModelScope.launch {
            val params = AdminBundleParams(
                title = title,
                slug = slug,
                description = description.ifBlank { null },
                productId = productId.toLongOrNull() ?: 0,
                memberProductIds = memberProductIds.split(",", "،").mapNotNull { it.trim().toLongOrNull() }
            )
            when (val result = createBundleUseCase(params)) {
                is AppResult.Success -> {
                    _effect.send(AdminBundlesEffect.ShowSuccess("باندل ساخته شد."))
                    load()
                }
                is AppResult.Error -> _effect.send(AdminBundlesEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    fun deleteBundle(id: Long) {
        viewModelScope.launch {
            when (val result = deleteBundleUseCase(id)) {
                is AppResult.Success -> {
                    _effect.send(AdminBundlesEffect.ShowSuccess("باندل حذف شد."))
                    load()
                }
                is AppResult.Error -> _effect.send(AdminBundlesEffect.ShowError(result.message))
                else -> {}
            }
        }
    }
}
