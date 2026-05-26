package com.kazemieh.admin.options

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.admin.AdminOption
import com.kazemieh.domain.usecase.admin.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ManageOptionsViewModel(
    private val getAdminOptionsUseCase: GetAdminOptionsUseCase,
    private val createOptionTypeUseCase: CreateOptionTypeUseCase,
    private val updateOptionTypeUseCase: UpdateOptionTypeUseCase,
    private val deleteOptionTypeUseCase: DeleteOptionTypeUseCase,
    private val createOptionValueUseCase: CreateOptionValueUseCase,
    private val updateOptionValueUseCase: UpdateOptionValueUseCase,
    private val deleteOptionValueUseCase: DeleteOptionValueUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ManageOptionsState())
    val state = _state.asStateFlow()

    private val _effect = Channel<ManageOptionsEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadOptions()
    }

    fun handleIntent(intent: ManageOptionsIntent) {
        when (intent) {
            is ManageOptionsIntent.LoadOptions -> loadOptions()
            is ManageOptionsIntent.CreateOptionType -> createOptionType(intent.name)
            is ManageOptionsIntent.UpdateOptionType -> updateOptionType(intent.id, intent.name)
            is ManageOptionsIntent.DeleteOptionType -> deleteOptionType(intent.id)
            is ManageOptionsIntent.CreateOptionValue -> createOptionValue(intent.optionTypeId, intent.value)
            is ManageOptionsIntent.UpdateOptionValue -> updateOptionValue(intent.id, intent.optionTypeId, intent.value)
            is ManageOptionsIntent.DeleteOptionValue -> deleteOptionValue(intent.id)
        }
    }

    private fun loadOptions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = getAdminOptionsUseCase()) {
                is AppResult.Success -> {
                    _state.update { it.copy(isLoading = false, options = result.data) }
                }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(ManageOptionsEffect.ShowError(result.message))
                }
                is AppResult.Loading -> {}
            }
        }
    }

    private fun createOptionType(name: String) {
        viewModelScope.launch {
            when (val result = createOptionTypeUseCase(name)) {
                is AppResult.Success -> loadOptions()
                is AppResult.Error -> _effect.send(ManageOptionsEffect.ShowError(result.message))
                is AppResult.Loading -> {}
            }
        }
    }

    private fun updateOptionType(id: Long, name: String) {
        viewModelScope.launch {
            when (val result = updateOptionTypeUseCase(id, name)) {
                is AppResult.Success -> loadOptions()
                is AppResult.Error -> _effect.send(ManageOptionsEffect.ShowError(result.message))
                is AppResult.Loading -> {}
            }
        }
    }

    private fun deleteOptionType(id: Long) {
        viewModelScope.launch {
            when (val result = deleteOptionTypeUseCase(id)) {
                is AppResult.Success -> loadOptions()
                is AppResult.Error -> _effect.send(ManageOptionsEffect.ShowError(result.message))
                is AppResult.Loading -> {}
            }
        }
    }

    private fun createOptionValue(optionTypeId: Long, value: String) {
        viewModelScope.launch {
            when (val result = createOptionValueUseCase(optionTypeId, value)) {
                is AppResult.Success -> loadOptions()
                is AppResult.Error -> _effect.send(ManageOptionsEffect.ShowError(result.message))
                is AppResult.Loading -> {}
            }
        }
    }

    private fun updateOptionValue(id: Long, optionTypeId: Long, value: String) {
        viewModelScope.launch {
            when (val result = updateOptionValueUseCase(id, optionTypeId, value)) {
                is AppResult.Success -> loadOptions()
                is AppResult.Error -> _effect.send(ManageOptionsEffect.ShowError(result.message))
                is AppResult.Loading -> {}
            }
        }
    }

    private fun deleteOptionValue(id: Long) {
        viewModelScope.launch {
            when (val result = deleteOptionValueUseCase(id)) {
                is AppResult.Success -> loadOptions()
                is AppResult.Error -> _effect.send(ManageOptionsEffect.ShowError(result.message))
                is AppResult.Loading -> {}
            }
        }
    }
}

data class ManageOptionsState(
    val isLoading: Boolean = false,
    val options: List<AdminOption> = emptyList()
)

sealed interface ManageOptionsIntent {
    data object LoadOptions : ManageOptionsIntent
    data class CreateOptionType(val name: String) : ManageOptionsIntent
    data class UpdateOptionType(val id: Long, val name: String) : ManageOptionsIntent
    data class DeleteOptionType(val id: Long) : ManageOptionsIntent
    data class CreateOptionValue(val optionTypeId: Long, val value: String) : ManageOptionsIntent
    data class UpdateOptionValue(val id: Long, val optionTypeId: Long, val value: String) : ManageOptionsIntent
    data class DeleteOptionValue(val id: Long) : ManageOptionsIntent
}

sealed interface ManageOptionsEffect {
    data class ShowError(val message: Any) : ManageOptionsEffect
    data class ShowSuccess(val message: Any) : ManageOptionsEffect
}
