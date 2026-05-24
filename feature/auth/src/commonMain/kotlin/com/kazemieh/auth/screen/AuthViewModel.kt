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
import com.kazemieh.domain.usecase.ResetPasswordUseCase
import com.kazemieh.domain.validation.ValidateEmail
import com.kazemieh.domain.validation.ValidatePassword
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
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
                state = state.copy(email = event.value, emailError = null)
            }

            is AuthEvent.OnPasswordChange -> {
                state = state.copy(password = event.value, passwordError = null)
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

            is AuthEvent.OnNewPasswordChange -> {
                state = state.copy(newPassword = event.value, newPasswordError = null)
            }

            is AuthEvent.OnConfirmPasswordChange -> {
                state = state.copy(confirmPassword = event.value, confirmPasswordError = null)
            }

            is AuthEvent.OnTokenReceived -> {
                state = state.copy(token = event.value)
            }

            AuthEvent.SubmitResetPassword -> {
                submitResetPassword()
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
                _event.emit(UiEvent.NavigateBack)
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
                _event.emit(UiEvent.NavigateBack)
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
                _event.emit(UiEvent.ShowSuccess(Unit))
            }.doOnError {
                state = state.copy(isLoading = false)
                _event.emit(UiEvent.ShowError(it))
            }

        }
    }

    private fun submitResetPassword() {
        val passwordResult = validatePassword(state.newPassword)
        if (!passwordResult.successful) {
            state = state.copy(newPasswordError = passwordResult.errorMessage)
            return
        }

        if (state.newPassword != state.confirmPassword) {
            state = state.copy(confirmPasswordError = "PASSWORDS_DO_NOT_MATCH")
            return
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true)

            val result = resetPasswordUseCase(state.token, state.newPassword)

            result.doOnSuccess {
                state = state.copy(isLoading = false)
                _event.emit(UiEvent.ShowSuccess(Unit))
                _event.emit(UiEvent.NavigateToLogin)
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

    val newPassword: String = "",
    val confirmPassword: String = "",
    val token: String = "",

    val emailError: Any? = null,
    val passwordError: Any? = null,
    val newPasswordError: Any? = null,
    val confirmPasswordError: Any? = null,

    val isLoading: Boolean = false,
)

sealed class AuthEvent {
    data class OnEmailChange(val value: String) : AuthEvent()
    data class OnPasswordChange(val value: String) : AuthEvent()

    data class OnNewPasswordChange(val value: String) : AuthEvent()
    data class OnConfirmPasswordChange(val value: String) : AuthEvent()
    data class OnTokenReceived(val value: String) : AuthEvent()

    object SubmitLogin : AuthEvent()
    object SubmitRegister : AuthEvent()
    object SubmitForgotPassword : AuthEvent()
    object SubmitResetPassword : AuthEvent()
}

sealed class UiEvent {
    object NavigateToHome : UiEvent()
    object NavigateBack : UiEvent()
    object NavigateToLogin : UiEvent()
    data class ShowError(val message: Any?) : UiEvent()
    data class ShowSuccess(val message: Any?) : UiEvent()
}
