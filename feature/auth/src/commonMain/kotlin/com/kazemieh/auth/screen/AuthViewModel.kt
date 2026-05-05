package com.kazemieh.auth.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.doOnError
import com.kazemieh.common.doOnSuccess
import com.kazemieh.domain.usecase.ForgotPasswordUseCase
import com.kazemieh.domain.usecase.LoginUseCase
import com.kazemieh.domain.usecase.RegisterUseCase
import com.kazemieh.domain.validation.ValidateEmail
import com.kazemieh.domain.validation.ValidatePassword
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val validateEmail: ValidateEmail,
    private val validatePassword: ValidatePassword
) : ViewModel() {

    var state by mutableStateOf(AuthState())
        private set

    private val _event = MutableSharedFlow<UiEvent>()
    val event = _event.asSharedFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {

            is AuthEvent.OnEmailChange -> {
                state = state.copy(email = event.value)
            }

            is AuthEvent.OnPasswordChange -> {
                state = state.copy(password = event.value)
            }

            AuthEvent.SubmitLogin -> {
                submitLogin()
            }

            AuthEvent.SubmitRegister -> {
                submitRegister()
            }

            AuthEvent.SubmitForgotPassword -> {
                submitForgot()
            }
        }
    }

    private fun submitLogin() {
        val emailResult = validateEmail(state.email)
        val passwordResult = validatePassword(state.password)

        if (!emailResult.successful || !passwordResult.successful) {
            state = state.copy(
                emailError = emailResult.errorMessage,
                passwordError = passwordResult.errorMessage
            )
            return
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val result = loginUseCase(state.email, state.password)

            result.doOnSuccess {
                state = state.copy(isLoading = false)
                _event.emit(UiEvent.NavigateToHome)
            }.doOnError {
                state = state.copy(isLoading = false)
                _event.emit(UiEvent.ShowError(it))
            }

        }
    }

    private fun submitRegister() {
        val emailResult = validateEmail(state.email)
        val passwordResult = validatePassword(state.password)

        if (!emailResult.successful || !passwordResult.successful) {
            state = state.copy(
                emailError = emailResult.errorMessage,
                passwordError = passwordResult.errorMessage
            )
            return
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val result = registerUseCase(state.email, state.password)

            result.doOnSuccess {
                state = state.copy(isLoading = false)
                _event.emit(UiEvent.NavigateToHome)
            }.doOnError {
                state = state.copy(isLoading = false)
                _event.emit(UiEvent.ShowError(it))
            }
        }
    }

    private fun submitForgot() {
        val emailResult = validateEmail(state.email)

        if (!emailResult.successful) {
            state = state.copy(emailError = emailResult.errorMessage)
            return
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val result = forgotPasswordUseCase(state.email)

            result.doOnSuccess {
                state = state.copy(isLoading = false)
                _event.emit(UiEvent.NavigateToHome)
            }.doOnError {
                state = state.copy(isLoading = false)
                _event.emit(UiEvent.ShowError(it))
            }

        }
    }
}

data class AuthState(
    val email: String = "",
    val password: String = "",

    val emailError: String? = null,
    val passwordError: String? = null,

    val isLoading: Boolean = false,
)

sealed class AuthEvent {
    data class OnEmailChange(val value: String) : AuthEvent()
    data class OnPasswordChange(val value: String) : AuthEvent()

    object SubmitLogin : AuthEvent()
    object SubmitRegister : AuthEvent()
    object SubmitForgotPassword : AuthEvent()
}

sealed class UiEvent {
    object NavigateToHome : UiEvent()
    data class ShowError(val message: String?) : UiEvent()
}