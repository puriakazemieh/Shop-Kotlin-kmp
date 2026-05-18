package com.kazemieh.home.cart.checkout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.AppResult
import com.kazemieh.common.getSuccessValue
import com.kazemieh.domain.model.Cart
import com.kazemieh.domain.usecase.GetProfileUseCase
import com.kazemieh.domain.usecase.cart.GetCartUseCase
import com.kazemieh.domain.usecase.order.CreateOrderUseCase
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
    val country: String = "Iran"
)

data class PhoneNumber(val number: String)

class CheckoutViewModel(
    private val createOrderUseCase: CreateOrderUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val getCartUseCase: GetCartUseCase
) : ViewModel() {

    var screenState by mutableStateOf(CheckoutScreenState())
        private set

    var isFormValid by mutableStateOf(false)
        private set

    private var cart: Cart? = null

    init {
        loadProfile()
        loadCart()
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
            cart = getCartUseCase().first().getSuccessValue()
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

    private fun validateForm() {
        isFormValid = screenState.firstName.isNotBlank() &&
                screenState.lastName.isNotBlank() &&
                screenState.email.isNotBlank() &&
                screenState.city.isNotBlank() &&
                screenState.address.isNotBlank()
    }

    fun payWithPayPal(onSuccess: () -> Unit, onError: (String) -> Unit) {
        // PayPal implementation would go here
        onError("PayPal is not implemented yet.")
    }

    fun payOnDelivery(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val items = cart?.items?.map { it.variantId to it.qty } ?: emptyList()
            if (items.isEmpty()) {
                onError("Cart is empty")
                return@launch
            }

            val result = createOrderUseCase(items)
            when (result) {
                is AppResult.Success -> onSuccess()
                is AppResult.Error -> onError(result.message)
                else -> {}
            }
        }
    }
}
