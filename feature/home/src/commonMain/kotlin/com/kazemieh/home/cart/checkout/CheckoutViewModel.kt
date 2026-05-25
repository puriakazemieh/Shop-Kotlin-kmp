package com.kazemieh.home.cart.checkout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class CheckoutScreenState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val city: String = "",
    val postalCode: String = "",
    val address: String = "",
    val phoneNumber: PhoneNumber? = null,
    val country: String = "Iran",
    val addresses: List<Address> = emptyList(),
    val selectedAddressId: Long? = null,
    val showAddNewAddressForm: Boolean = false,
    val isLoading: Boolean = false
)

data class PhoneNumber(val number: String)

class CheckoutViewModel(
    private val createOrderUseCase: CreateOrderUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val getCartUseCase: GetCartUseCase,
    private val addAddressUseCase: AddAddressUseCase,
    private val getAddressesUseCase: GetAddressesUseCase,
    private val requestPaymentUseCase: RequestPaymentUseCase
) : ViewModel() {

    var screenState by mutableStateOf(CheckoutScreenState())
        private set

    var isFormValid by mutableStateOf(false)
        private set

    private var cart: Cart? = null

    init {
        loadProfile()
        loadCart()
        loadAddresses()
    }

    private fun loadAddresses() {
        viewModelScope.launch {
            screenState = screenState.copy(isLoading = true)
            val result = getAddressesUseCase()
            result.getSuccessValue()?.let { addresses ->
                screenState = screenState.copy(
                    addresses = addresses,
                    selectedAddressId = addresses.find { it.isDefault }?.id ?: addresses.firstOrNull()?.id,
                    showAddNewAddressForm = addresses.isEmpty(),
                    isLoading = false
                )
            } ?: run {
                screenState = screenState.copy(isLoading = false, showAddNewAddressForm = true)
            }
            validateForm()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val profileResult = getProfileUseCase()
            profileResult.getSuccessValue()?.let { profile ->
                screenState = screenState.copy(
                    firstName = profile.firstName ?: "",
                    lastName = profile.lastName ?: "",
                    email = profile.email,
                    city = profile.city ?: "",
                    postalCode = profile.postalCode?.toString() ?: "",
                    phoneNumber = profile.phone?.let { PhoneNumber(it) }
                )
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

    fun updateFirstName(value: String) {
        screenState = screenState.copy(firstName = value)
        validateForm()
    }

    fun updateLastName(value: String) {
        screenState = screenState.copy(lastName = value)
        validateForm()
    }

    fun updateCity(value: String) {
        screenState = screenState.copy(city = value)
        validateForm()
    }

    fun updatePostalCode(value: String) {
        screenState = screenState.copy(postalCode = value)
        validateForm()
    }

    fun updateAddress(value: String) {
        screenState = screenState.copy(address = value)
        validateForm()
    }

    fun updatePhoneNumber(value: String) {
        screenState = screenState.copy(phoneNumber = PhoneNumber(value))
        validateForm()
    }

    fun updateCountry(value: String) {
        screenState = screenState.copy(country = value)
        validateForm()
    }

    fun selectAddress(addressId: Long) {
        screenState = screenState.copy(selectedAddressId = addressId, showAddNewAddressForm = false)
        validateForm()
    }

    fun toggleAddNewAddress(show: Boolean) {
        screenState = screenState.copy(showAddNewAddressForm = show)
        validateForm()
    }

    fun addNewAddress(
        receiverName: String,
        receiverPhone: String,
        province: String,
        city: String,
        addressLine1: String,
        addressLine2: String?,
        postalCode: String?,
        onSuccess: () -> Unit,
        onError: (Any) -> Unit
    ) {
        viewModelScope.launch {
            val result = addAddressUseCase(
                receiverName = receiverName,
                receiverPhone = receiverPhone,
                country = screenState.country,
                province = province,
                city = city,
                addressLine1 = addressLine1,
                addressLine2 = addressLine2,
                postalCode = postalCode,
                setAsDefault = true
            )
            when (result) {
                is AppResult.Success -> {
                    loadAddresses()
                    onSuccess()
                }
                is AppResult.Error -> onError(result.message)
                else -> {}
            }
        }
    }

    private fun validateForm() {
        isFormValid = if (screenState.showAddNewAddressForm) {
            screenState.firstName.isNotBlank() &&
                    screenState.lastName.isNotBlank() &&
                    screenState.email.isNotBlank() &&
                    screenState.city.isNotBlank() &&
                    screenState.address.isNotBlank()
        } else {
            screenState.selectedAddressId != null
        }
    }

    fun payWithZarinpal(onSuccess: (String) -> Unit, onError: (Any) -> Unit) {
        viewModelScope.launch {
            val items = cart?.items?.map { it.variantId to it.qty } ?: emptyList()
            if (items.isEmpty()) {
                onError(Resources.String.CartIsEmptyError)
                return@launch
            }

            val addressId = screenState.selectedAddressId
            if (addressId == null) {
                onError(Resources.String.SelectAddressError)
                return@launch
            }

            screenState = screenState.copy(isLoading = true)

            // 1. Create Order
            val orderResult = createOrderUseCase(items, addressId)
            when (orderResult) {
                is AppResult.Success -> {
                    val orderId = orderResult.data.id
                    // 2. Request Payment URL
                    val paymentResult = requestPaymentUseCase(orderId)
                    when (paymentResult) {
                        is AppResult.Success -> {
                            screenState = screenState.copy(isLoading = false)
                            onSuccess(paymentResult.data)
                        }
                        is AppResult.Error -> {
                            screenState = screenState.copy(isLoading = false)
                            onError(paymentResult.message)
                        }
                        else -> {}
                    }
                }
                is AppResult.Error -> {
                    screenState = screenState.copy(isLoading = false)
                    onError(orderResult.message)
                }
                else -> {}
            }
        }
    }

    fun payOnDelivery(onSuccess: () -> Unit, onError: (Any) -> Unit) {
        viewModelScope.launch {
            val items = cart?.items?.map { it.variantId to it.qty } ?: emptyList()
            if (items.isEmpty()) {
                onError(Resources.String.CartIsEmptyError)
                return@launch
            }

            val addressId = screenState.selectedAddressId

            if (addressId == null) {
                onError(Resources.String.SelectAddressError)
                return@launch
            }

            // Create Order
            val result = createOrderUseCase(items, addressId)
            when (result) {
                is AppResult.Success -> {
                    CartEventBus.refresh()
                    onSuccess()
                }
                is AppResult.Error -> onError(result.message)
                else -> {}
            }
        }
    }
}
