package com.kazemieh.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.domain.model.Address
import com.kazemieh.domain.model.Profile
import com.kazemieh.domain.usecase.GetProfileUseCase
import com.kazemieh.domain.usecase.ObserveProfileUseCase
import com.kazemieh.domain.usecase.UpdateProfileUseCase
import com.kazemieh.domain.usecase.address.AddAddressUseCase
import com.kazemieh.domain.usecase.address.DeleteAddressUseCase
import com.kazemieh.domain.usecase.address.GetAddressesUseCase
import com.kazemieh.domain.usecase.address.SetDefaultAddressUseCase
import com.kazemieh.domain.usecase.address.UpdateAddressUseCase
import com.kazemieh.domain.validation.ValidateProfileUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.kazemieh.designsystem.Resources

class ProfileViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val observeProfileUseCase: ObserveProfileUseCase,
    private val validateProfileUseCase: ValidateProfileUseCase,
    private val getAddressesUseCase: GetAddressesUseCase,
    private val addAddressUseCase: AddAddressUseCase,
    private val updateAddressUseCase: UpdateAddressUseCase,
    private val deleteAddressUseCase: DeleteAddressUseCase,
    private val setDefaultAddressUseCase: SetDefaultAddressUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    private val _event = Channel<UiEvent>()
    val event = _event.receiveAsFlow()

    init {
        handleIntent(ProfileIntent.LoadProfile)
        handleIntent(ProfileIntent.LoadAddresses)
        observeProfile()
    }

    fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.LoadProfile -> loadProfile()
            is ProfileIntent.UpdateFirstName -> updateFirstName(intent.value)
            is ProfileIntent.UpdateLastName -> updateLastName(intent.value)
            is ProfileIntent.UpdateCity -> updateCity(intent.value)
            is ProfileIntent.UpdatePostalCode -> updatePostalCode(intent.value)
            is ProfileIntent.UpdateAddress -> {} // This was for profile address, now we have a list
            is ProfileIntent.UpdatePhoneNumber -> updatePhoneNumber(intent.value)
            is ProfileIntent.SaveProfile -> saveProfile()
            
            is ProfileIntent.LoadAddresses -> loadAddresses()
            is ProfileIntent.AddAddress -> addAddress(intent)
            is ProfileIntent.UpdateUserAddress -> updateUserAddress(intent)
            is ProfileIntent.DeleteAddress -> deleteAddress(intent.id)
            is ProfileIntent.SetDefaultAddress -> setDefaultAddress(intent.id)
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

    private fun loadAddresses() {
        viewModelScope.launch {
            _state.update { it.copy(addressLoading = true) }
            when (val result = getAddressesUseCase()) {
                is AppResult.Success -> {
                    _state.update { it.copy(addresses = result.data, addressLoading = false) }
                }
                is AppResult.Error -> {
                    _state.update { it.copy(addressLoading = false) }
                    _event.send(UiEvent.ShowError(result.message))
                }
                is AppResult.Loading -> {}
            }
        }
    }

    private fun addAddress(intent: ProfileIntent.AddAddress) {
        viewModelScope.launch {
            _state.update { it.copy(addressSaving = true) }
            val result = addAddressUseCase(
                receiverName = intent.receiverName,
                receiverPhone = intent.receiverPhone,
                country = intent.country,
                province = intent.province,
                city = intent.city,
                addressLine1 = intent.addressLine1,
                addressLine2 = intent.addressLine2,
                postalCode = intent.postalCode,
                setAsDefault = intent.setAsDefault
            )
            when (result) {
                is AppResult.Success -> {
                    _event.send(UiEvent.ShowSuccess(Resources.String.AddressAddedSuccessfully))
                    loadAddresses()
                }
                is AppResult.Error -> {
                    _event.send(UiEvent.ShowError(result.message))
                }
                is AppResult.Loading -> {}
            }
            _state.update { it.copy(addressSaving = false) }
        }
    }

    private fun updateUserAddress(intent: ProfileIntent.UpdateUserAddress) {
        viewModelScope.launch {
            _state.update { it.copy(addressSaving = true) }
            val result = updateAddressUseCase(
                id = intent.id,
                receiverName = intent.receiverName,
                receiverPhone = intent.receiverPhone,
                country = intent.country,
                province = intent.province,
                city = intent.city,
                addressLine1 = intent.addressLine1,
                addressLine2 = intent.addressLine2,
                postalCode = intent.postalCode
            )
            when (result) {
                is AppResult.Success -> {
                    _event.send(UiEvent.ShowSuccess(Resources.String.VariantUpdated))
                    loadAddresses()
                }
                is AppResult.Error -> {
                    _event.send(UiEvent.ShowError(result.message))
                }
                is AppResult.Loading -> {}
            }
            _state.update { it.copy(addressSaving = false) }
        }
    }

    private fun deleteAddress(id: Long) {
        viewModelScope.launch {
            when (val result = deleteAddressUseCase(id)) {
                is AppResult.Success -> {
                    _event.send(UiEvent.ShowSuccess(Resources.String.VariantDeleted))
                    loadAddresses()
                }
                is AppResult.Error -> {
                    _event.send(UiEvent.ShowError(result.message))
                }
                is AppResult.Loading -> {}
            }
        }
    }

    private fun setDefaultAddress(id: Long) {
        viewModelScope.launch {
            when (val result = setDefaultAddressUseCase(id)) {
                is AppResult.Success -> {
                    _event.send(UiEvent.ShowSuccess(Resources.String.StatusUpdatedSuccessfully))
                    loadAddresses()
                }
                is AppResult.Error -> {
                    _event.send(UiEvent.ShowError(result.message))
                }
                is AppResult.Loading -> {}
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
                    _event.send(UiEvent.ShowSuccess(Resources.String.Success))
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

    data object LoadAddresses : ProfileIntent
    data class AddAddress(
        val receiverName: String,
        val receiverPhone: String,
        val country: String,
        val province: String,
        val city: String,
        val addressLine1: String,
        val addressLine2: String?,
        val postalCode: String?,
        val setAsDefault: Boolean
    ) : ProfileIntent
    data class UpdateUserAddress(
        val id: Long,
        val receiverName: String?,
        val receiverPhone: String?,
        val country: String?,
        val province: String?,
        val city: String?,
        val addressLine1: String?,
        val addressLine2: String?,
        val postalCode: String?
    ) : ProfileIntent
    data class DeleteAddress(val id: Long) : ProfileIntent
    data class SetDefaultAddress(val id: Long) : ProfileIntent
}

data class ProfileState(
    val profile: Profile? = null,
    val addresses: List<Address> = emptyList(),
    val displayState: AppResult<Unit?> = AppResult.Loading,
    val isFormValid: Boolean = false,
    val isSaving: Boolean = false,
    val addressLoading: Boolean = false,
    val addressSaving: Boolean = false
)

sealed class UiEvent {
    data class ShowError(val message: Any) : UiEvent()
    data class ShowSuccess(val message: Any) : UiEvent()
}
