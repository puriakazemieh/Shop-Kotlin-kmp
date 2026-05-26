package com.kazemieh.auth.screen

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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val validateEmail: ValidateEmail,
    private val validatePassword: ValidatePassword
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AuthEffect>()
    val effect = _effect.receiveAsFlow()

    fun handleIntent(event: AuthIntent) {
        when (event) {

            is AuthIntent.OnEmailChange -> {
                _state.update { it.copy(email = event.value, emailError = null) }
            }

            is AuthIntent.OnPasswordChange -> {
                _state.update { it.copy(password = event.value, passwordError = null) }
            }

            AuthIntent.SubmitLogin -> {
                submitLogin()
            }

            AuthIntent.SubmitRegister -> {
                submitRegister()
            }

            AuthIntent.SubmitForgotPassword -> {
                submitForgot()
            }

            is AuthIntent.OnNewPasswordChange -> {
                _state.update { it.copy(newPassword = event.value, newPasswordError = null) }
            }

            is AuthIntent.OnConfirmPasswordChange -> {
                _state.update { it.copy(confirmPassword = event.value, confirmPasswordError = null) }
            }

            is AuthIntent.OnTokenReceived -> {
                _state.update { it.copy(token = event.value) }
            }

            AuthIntent.SubmitResetPassword -> {
                submitResetPassword()
            }
        }
    }

    private fun submitLogin() {
        val s = _state.value
        val emailResult = validateEmail(s.email)
        val passwordResult = validatePassword(s.password)

        if (!emailResult.successful || !passwordResult.successful) {
            _state.update {
                it.copy(
                    emailError = emailResult.errorMessage,
                    passwordError = passwordResult.errorMessage
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val result = loginUseCase(s.email, s.password)

            result.doOnSuccess {
                _state.update { it.copy(isLoading = false) }
                _effect.send(AuthEffect.NavigateBack)
            }.doOnError { err ->
                _state.update { it.copy(isLoading = false) }
                _effect.send(AuthEffect.ShowError(err))
            }

        }
    }

    private fun submitRegister() {
        val s = _state.value
        val emailResult = validateEmail(s.email)
        val passwordResult = validatePassword(s.password)

        if (!emailResult.successful || !passwordResult.successful) {
            _state.update {
                it.copy(
                    emailError = emailResult.errorMessage,
                    passwordError = passwordResult.errorMessage
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val result = registerUseCase(s.email, s.password)

            result.doOnSuccess {
                _state.update { it.copy(isLoading = false) }
                _effect.send(AuthEffect.NavigateBack)
            }.doOnError { err ->
                _state.update { it.copy(isLoading = false) }
                _effect.send(AuthEffect.ShowError(err))
            }
        }
    }

    private fun submitForgot() {
        val s = _state.value
        val emailResult = validateEmail(s.email)

        if (!emailResult.successful) {
            _state.update { it.copy(emailError = emailResult.errorMessage) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val result = forgotPasswordUseCase(s.email)

            result.doOnSuccess {
                _state.update { it.copy(isLoading = false) }
                _effect.send(AuthEffect.ShowSuccess(Unit))
            }.doOnError { err ->
                _state.update { it.copy(isLoading = false) }
                _effect.send(AuthEffect.ShowError(err))
            }

        }
    }

    private fun submitResetPassword() {
        val s = _state.value
        val passwordResult = validatePassword(s.newPassword)
        if (!passwordResult.successful) {
            _state.update { it.copy(newPasswordError = passwordResult.errorMessage) }
            return
        }

        if (s.newPassword != s.confirmPassword) {
            _state.update { it.copy(confirmPasswordError = "PASSWORDS_DO_NOT_MATCH") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val result = resetPasswordUseCase(s.token, s.newPassword)

            result.doOnSuccess {
                _state.update { it.copy(isLoading = false) }
                _effect.send(AuthEffect.ShowSuccess(Unit))
                _effect.send(AuthEffect.NavigateToLogin)
            }.doOnError { err ->
                _state.update { it.copy(isLoading = false) }
                _effect.send(AuthEffect.ShowError(err))
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

sealed class AuthIntent {
    data class OnEmailChange(val value: String) : AuthIntent()
    data class OnPasswordChange(val value: String) : AuthIntent()

    data class OnNewPasswordChange(val value: String) : AuthIntent()
    data class OnConfirmPasswordChange(val value: String) : AuthIntent()
    data class OnTokenReceived(val value: String) : AuthIntent()

    object SubmitLogin : AuthIntent()
    object SubmitRegister : AuthIntent()
    object SubmitForgotPassword : AuthIntent()
    object SubmitResetPassword : AuthIntent()
}

sealed class AuthEffect {
    object NavigateToHome : AuthEffect()
    object NavigateBack : AuthEffect()
    object NavigateToLogin : AuthEffect()
    data class ShowError(val message: Any?) : AuthEffect()
    data class ShowSuccess(val message: Any?) : AuthEffect()
}
