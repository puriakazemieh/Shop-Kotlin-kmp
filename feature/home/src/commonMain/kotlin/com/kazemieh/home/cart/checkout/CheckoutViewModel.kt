package com.kazemieh.home.cart.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.common.CartEventBus
import com.kazemieh.common.getSuccessValue
import com.kazemieh.designsystem.Resources
import com.kazemieh.domain.model.Address
import com.kazemieh.domain.model.Cart
import com.kazemieh.domain.usecase.GetProfileUseCase
import com.kazemieh.domain.usecase.address.AddAddressUseCase
import com.kazemieh.domain.usecase.address.GetAddressesUseCase
import com.kazemieh.domain.usecase.cart.GetCartUseCase
import com.kazemieh.domain.usecase.order.CreateOrderUseCase
import com.kazemieh.domain.usecase.payment.RequestPaymentUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CheckoutState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val city: String = "",
    val postalCode: String = "",
    val address: String = "",
    val phoneNumber: String = "",
    val country: String = "Iran",
    val addresses: List<Address> = emptyList(),
    val selectedAddressId: Long? = null,
    val showAddNewAddressForm: Boolean = false,
    val isLoading: Boolean = false,
    val isFormValid: Boolean = false
)

sealed interface CheckoutIntent {
    data class UpdateFirstName(val value: String) : CheckoutIntent
    data class UpdateLastName(val value: String) : CheckoutIntent
    data class UpdateCity(val value: String) : CheckoutIntent
    data class UpdatePostalCode(val value: String) : CheckoutIntent
    data class UpdateAddress(val value: String) : CheckoutIntent
    data class UpdatePhoneNumber(val value: String) : CheckoutIntent
    data class UpdateCountry(val value: String) : CheckoutIntent
    data class SelectAddress(val addressId: Long) : CheckoutIntent
    data class ToggleAddNewAddress(val show: Boolean) : CheckoutIntent
    data class AddNewAddress(
        val receiverName: String,
        val receiverPhone: String,
        val province: String,
        val city: String,
        val addressLine1: String,
        val addressLine2: String?,
        val postalCode: String?
    ) : CheckoutIntent
    data object PayWithZarinpal : CheckoutIntent
    data object PayOnDelivery : CheckoutIntent
}

sealed interface CheckoutEffect {
    data class NavigateToPaymentCompleted(val success: Boolean, val error: String?) : CheckoutEffect
    data class OpenZarinpal(val url: String) : CheckoutEffect
    data class ShowError(val message: Any) : CheckoutEffect
    data object AddressAdded : CheckoutEffect
}

class CheckoutViewModel(
    private val createOrderUseCase: CreateOrderUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val getCartUseCase: GetCartUseCase,
    private val addAddressUseCase: AddAddressUseCase,
    private val getAddressesUseCase: GetAddressesUseCase,
    private val requestPaymentUseCase: RequestPaymentUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CheckoutState())
    val state = _state.asStateFlow()

    private val _effect = Channel<CheckoutEffect>()
    val effect = _effect.receiveAsFlow()

    private var cart: Cart? = null

    init {
        loadProfile()
        loadCart()
        loadAddresses()
    }

    fun handleIntent(intent: CheckoutIntent) {
        when (intent) {
            is CheckoutIntent.UpdateFirstName -> {
                _state.update { it.copy(firstName = intent.value) }
                validateForm()
            }
            is CheckoutIntent.UpdateLastName -> {
                _state.update { it.copy(lastName = intent.value) }
                validateForm()
            }
            is CheckoutIntent.UpdateCity -> {
                _state.update { it.copy(city = intent.value) }
                validateForm()
            }
            is CheckoutIntent.UpdatePostalCode -> {
                _state.update { it.copy(postalCode = intent.value) }
                validateForm()
            }
            is CheckoutIntent.UpdateAddress -> {
                _state.update { it.copy(address = intent.value) }
                validateForm()
            }
            is CheckoutIntent.UpdatePhoneNumber -> {
                _state.update { it.copy(phoneNumber = intent.value) }
                validateForm()
            }
            is CheckoutIntent.UpdateCountry -> {
                _state.update { it.copy(country = intent.value) }
                validateForm()
            }
            is CheckoutIntent.SelectAddress -> {
                _state.update { it.copy(selectedAddressId = intent.addressId, showAddNewAddressForm = false) }
                validateForm()
            }
            is CheckoutIntent.ToggleAddNewAddress -> {
                _state.update { it.copy(showAddNewAddressForm = intent.show) }
                validateForm()
            }
            is CheckoutIntent.AddNewAddress -> addNewAddress(intent)
            CheckoutIntent.PayWithZarinpal -> payWithZarinpal()
            CheckoutIntent.PayOnDelivery -> payOnDelivery()
        }
    }

    private fun loadAddresses() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = getAddressesUseCase()
            result.getSuccessValue()?.let { addresses ->
                _state.update {
                    it.copy(
                        addresses = addresses,
                        selectedAddressId = addresses.find { a -> a.isDefault }?.id ?: addresses.firstOrNull()?.id,
                        showAddNewAddressForm = addresses.isEmpty(),
                        isLoading = false
                    )
                }
            } ?: run {
                _state.update { it.copy(isLoading = false, showAddNewAddressForm = true) }
            }
            validateForm()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val profileResult = getProfileUseCase()
            profileResult.getSuccessValue()?.let { profile ->
                _state.update {
                    it.copy(
                        firstName = profile.firstName ?: "",
                        lastName = profile.lastName ?: "",
                        email = profile.email,
                        city = profile.city ?: "",
                        postalCode = profile.postalCode?.toString() ?: "",
                        phoneNumber = profile.phone ?: ""
                    )
                }
                validateForm()
            }
        }
    }

    private fun loadCart() {
        viewModelScope.launch {
            getCartUseCase().collect { result ->
                cart = result.getSuccessValue()
            }
        }
    }

    private fun addNewAddress(intent: CheckoutIntent.AddNewAddress) {
        viewModelScope.launch {
            val result = addAddressUseCase(
                receiverName = intent.receiverName,
                receiverPhone = intent.receiverPhone,
                country = _state.value.country,
                province = intent.province,
                city = intent.city,
                addressLine1 = intent.addressLine1,
                addressLine2 = intent.addressLine2,
                postalCode = intent.postalCode,
                setAsDefault = true
            )
            when (result) {
                is AppResult.Success -> {
                    loadAddresses()
                    _effect.send(CheckoutEffect.AddressAdded)
                }
                is AppResult.Error -> _effect.send(CheckoutEffect.ShowError(result.message))
                else -> {}
            }
        }
    }

    private fun validateForm() {
        val s = _state.value
        val isValid = if (s.showAddNewAddressForm) {
            s.firstName.isNotBlank() &&
                    s.lastName.isNotBlank() &&
                    s.email.isNotBlank() &&
                    s.city.isNotBlank() &&
                    s.address.isNotBlank()
        } else {
            s.selectedAddressId != null
        }
        _state.update { it.copy(isFormValid = isValid) }
    }

    private fun payWithZarinpal() {
        viewModelScope.launch {
            val items = cart?.items?.map { it.variantId to it.qty } ?: emptyList()
            if (items.isEmpty()) {
                _effect.send(CheckoutEffect.ShowError(Resources.String.CartIsEmptyError))
                return@launch
            }

            val addressId = _state.value.selectedAddressId
            if (addressId == null) {
                _effect.send(CheckoutEffect.ShowError(Resources.String.SelectAddressError))
                return@launch
            }

            _state.update { it.copy(isLoading = true) }

            val orderResult = createOrderUseCase(items, addressId)
            when (orderResult) {
                is AppResult.Success -> {
                    val orderId = orderResult.data.id
                    val paymentResult = requestPaymentUseCase(orderId)
                    when (paymentResult) {
                        is AppResult.Success -> {
                            _state.update { it.copy(isLoading = false) }
                            _effect.send(CheckoutEffect.OpenZarinpal(paymentResult.data))
                        }
                        is AppResult.Error -> {
                            _state.update { it.copy(isLoading = false) }
                            _effect.send(CheckoutEffect.ShowError(paymentResult.message))
                        }
                        else -> {}
                    }
                }
                is AppResult.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _effect.send(CheckoutEffect.ShowError(orderResult.message))
                }
                else -> {}
            }
        }
    }

    private fun payOnDelivery() {
        viewModelScope.launch {
            val items = cart?.items?.map { it.variantId to it.qty } ?: emptyList()
            if (items.isEmpty()) {
                _effect.send(CheckoutEffect.ShowError(Resources.String.CartIsEmptyError))
                return@launch
            }

            val addressId = _state.value.selectedAddressId

            if (addressId == null) {
                _effect.send(CheckoutEffect.ShowError(Resources.String.SelectAddressError))
                return@launch
            }

            val result = createOrderUseCase(items, addressId)
            when (result) {
                is AppResult.Success -> {
                    CartEventBus.refresh()
                    _effect.send(CheckoutEffect.NavigateToPaymentCompleted(true, null))
                }
                is AppResult.Error -> _effect.send(CheckoutEffect.ShowError(result.message))
                else -> {}
            }
        }
    }
}
