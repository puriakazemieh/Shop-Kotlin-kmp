package com.kazemieh.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.Profile
import com.kazemieh.domain.usecase.GetProfileUseCase
import com.kazemieh.domain.usecase.ObserveProfileUseCase
import com.kazemieh.domain.usecase.UpdateProfileUseCase
import com.kazemieh.domain.validation.ValidateProfileUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val observeProfileUseCase: ObserveProfileUseCase,
    private val validateProfileUseCase: ValidateProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    private val _event = Channel<UiEvent>()
    val event = _event.receiveAsFlow()

    init {
        handleIntent(ProfileIntent.LoadProfile)
        observeProfile()
    }

    fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.LoadProfile -> loadProfile()
            is ProfileIntent.UpdateFirstName -> updateFirstName(intent.value)
            is ProfileIntent.UpdateLastName -> updateLastName(intent.value)
            is ProfileIntent.UpdateCity -> updateCity(intent.value)
            is ProfileIntent.UpdatePostalCode -> updatePostalCode(intent.value)
            is ProfileIntent.UpdateAddress -> updateAddress(intent.value)
            is ProfileIntent.UpdatePhoneNumber -> updatePhoneNumber(intent.value)
            is ProfileIntent.SaveProfile -> saveProfile()
        }
    }

    private fun observeProfile() {
        viewModelScope.launch {
            observeProfileUseCase().collectLatest { result ->
                when (result) {
                    is AppResult.Success -> {
                        _state.update {
                            it.copy(
                                profile = result.data,
                                displayState = AppResult.Success(Unit),
                                isFormValid = validateProfileUseCase(result.data)
                            )
                        }
                    }

                    is AppResult.Error -> {
                        _state.update {
                            it.copy(displayState = AppResult.Error(result.message))
                        }
                    }

                    is AppResult.Loading -> {
                        _state.update {
                            it.copy(displayState = AppResult.Loading)
                        }
                    }
                }
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(displayState = AppResult.Loading) }

            when (val result = getProfileUseCase()) {
                is AppResult.Success -> {
                    _state.update {
                        it.copy(displayState = AppResult.Success(Unit), profile = result.data)
                    }
                }

                is AppResult.Error -> {
                    _state.update {
                        it.copy(displayState = AppResult.Error(result.message))
                    }
                }

                is AppResult.Loading -> {
                    _state.update {
                        it.copy(displayState = AppResult.Loading)
                    }
                }
            }
        }
    }

    private fun updateFirstName(value: String) {
        _state.value.profile?.let { profile ->
            val updated = profile.copy(firstName = value)
            _state.update {
                it.copy(
                    profile = updated,
                    isFormValid = validateProfileUseCase(updated)
                )
            }
        }
    }

    private fun updateLastName(value: String) {
        _state.value.profile?.let { profile ->
            val updated = profile.copy(lastName = value)
            _state.update {
                it.copy(
                    profile = updated,
                    isFormValid = validateProfileUseCase(updated)
                )
            }
        }
    }

    private fun updateCity(value: String) {
        _state.value.profile?.let { profile ->
            val updated = profile.copy(city = value)
            _state.update {
                it.copy(
                    profile = updated,
                    isFormValid = validateProfileUseCase(updated)
                )
            }
        }
    }

    private fun updatePostalCode(value: Int?) {
        _state.value.profile?.let { profile ->
            val updated = profile.copy(postalCode = value)
            _state.update {
                it.copy(
                    profile = updated,
                    isFormValid = validateProfileUseCase(updated)
                )
            }
        }
    }

    private fun updateAddress(value: String) {
//        _state.value.profile?.let { profile ->
//            val updated = profile.copy(address = value)
//            _state.update {
//                it.copy(
//                    profile = updated,
//                    isFormValid = validateProfileUseCase(updated)
//                )
//            }
//        }
    }

    private fun updatePhoneNumber(value: String) {
        _state.value.profile?.let { profile ->
            val updated = profile.copy(phone = value)
            _state.update {
                it.copy(
                    profile = updated,
                    isFormValid = validateProfileUseCase(updated)
                )
            }
        }
    }

    private fun saveProfile() {
        val profile = _state.value.profile ?: return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            when (val result = updateProfileUseCase(profile)) {
                is AppResult.Success -> {
                    _event.send(UiEvent.ShowSuccess("Successfully updated!"))
                }

                is AppResult.Error -> {
                    _event.send(UiEvent.ShowError(result.message))
                }

                is AppResult.Loading -> {}
            }

            _state.update { it.copy(isSaving = false) }
        }
    }
}


sealed interface ProfileIntent {
    data object LoadProfile : ProfileIntent
    data class UpdateFirstName(val value: String) : ProfileIntent
    data class UpdateLastName(val value: String) : ProfileIntent
    data class UpdateCity(val value: String) : ProfileIntent
    data class UpdatePostalCode(val value: Int?) : ProfileIntent
    data class UpdateAddress(val value: String) : ProfileIntent
    data class UpdatePhoneNumber(val value: String) : ProfileIntent
    data object SaveProfile : ProfileIntent
}

data class ProfileState(
    val profile: Profile? = null,
    val displayState: AppResult<Unit?> = AppResult.Loading,
    val isFormValid: Boolean = false,
    val isSaving: Boolean = false
)

sealed class UiEvent {
    data class ShowError(val message: String) : UiEvent()
    data class ShowSuccess(val message: String) : UiEvent()
}