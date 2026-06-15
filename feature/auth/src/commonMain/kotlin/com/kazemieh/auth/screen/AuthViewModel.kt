package com.kazemieh.auth.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazemieh.common.doOnError
import com.kazemieh.common.doOnSuccess
import com.kazemieh.domain.auth.ForgotPasswordUseCase
import com.kazemieh.domain.auth.LoginUseCase
import com.kazemieh.domain.auth.RegisterUseCase
import com.kazemieh.domain.auth.ResetPasswordUseCase
import com.kazemieh.domain.auth.ValidateEmail
import com.kazemieh.domain.auth.ValidatePassword
import com.kazemieh.domain.auth.ValidateMobile
import com.kazemieh.domain.auth.ValidateUsername
import com.kazemieh.domain.auth.SendLoginOtpUseCase
import com.kazemieh.domain.auth.LoginWithOtpUseCase
import com.kazemieh.domain.auth.ResetPasswordWithOtpUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
    private val sendLoginOtpUseCase: SendLoginOtpUseCase,
    private val loginWithOtpUseCase: LoginWithOtpUseCase,
    private val resetPasswordWithOtpUseCase: ResetPasswordWithOtpUseCase,
    private val validateEmail: ValidateEmail,
    private val validatePassword: ValidatePassword,
    private val validateMobile: ValidateMobile,
    private val validateUsername: ValidateUsername
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    private val _effect = Channel<AuthEffect>()
    val effect = _effect.receiveAsFlow()

    private var timerJob: Job? = null

    fun handleIntent(event: AuthIntent) {
        when (event) {

            is AuthIntent.OnEmailChange -> {
                _state.update { it.copy(email = event.value, emailError = null) }
            }

            is AuthIntent.OnPasswordChange -> {
                _state.update { it.copy(password = event.value, passwordError = null) }
            }

            is AuthIntent.OnMobileChange -> {
                _state.update { it.copy(mobile = event.value, mobileError = null) }
            }

            is AuthIntent.OnOtpChange -> {
                _state.update { it.copy(otp = event.value, otpError = null) }
            }

            AuthIntent.ToggleAuthMode -> {
                _state.update { it.copy(isOtpMode = !it.isOtpMode) }
            }

            AuthIntent.SubmitLogin -> {
                if (_state.value.isOtpMode) {
                    if (_state.value.otpSent) {
                        submitLoginWithOtp()
                    } else {
                        submitSendLoginOtp()
                    }
                } else {
                    submitLogin()
                }
            }

            AuthIntent.ResendOtp -> {
                submitSendLoginOtp()
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

    private fun startTimer() {
        timerJob?.cancel()
        _state.update { it.copy(resendTimer = 120) }
        timerJob = viewModelScope.launch {
            while (_state.value.resendTimer > 0) {
                delay(1000)
                _state.update { it.copy(resendTimer = it.resendTimer - 1) }
            }
        }
    }

    private fun submitSendLoginOtp() {
        val s = _state.value
        val mobileResult = validateMobile(s.mobile)

        if (!mobileResult.successful) {
            _state.update { it.copy(mobileError = mobileResult.errorMessage) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val result = sendLoginOtpUseCase(s.mobile)

            result.doOnSuccess {
                _state.update { it.copy(isLoading = false, otpSent = true) }
                startTimer()
                _effect.send(AuthEffect.ShowSuccess("OTP_SENT"))
            }.doOnError { err ->
                _state.update { it.copy(isLoading = false) }
                _effect.send(AuthEffect.ShowError(err))
            }
        }
    }

    private fun submitLoginWithOtp() {
        val s = _state.value
        if (s.otp.length < 4) { // Assuming 4-6 digits
            _state.update { it.copy(otpError = "INVALID_OTP") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val result = loginWithOtpUseCase(s.mobile, s.otp)

            result.doOnSuccess {
                _state.update { it.copy(isLoading = false) }
                _effect.send(AuthEffect.NavigateBack)
            }.doOnError { err ->
                _state.update { it.copy(isLoading = false) }
                _effect.send(AuthEffect.ShowError(err))
            }
        }
    }

    private fun submitLogin() {
        val s = _state.value
        val usernameResult = validateUsername(s.email) // email field is used as username in UI
        val passwordResult = validatePassword(s.password)

        if (!usernameResult.successful || !passwordResult.successful) {
            _state.update {
                it.copy(
                    emailError = usernameResult.errorMessage,
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
        val emailResult = if (!s.isOtpMode) validateEmail(s.email) else null
        val mobileResult = if (s.isOtpMode) validateMobile(s.mobile) else null
        val passwordResult = validatePassword(s.password)

        if ((emailResult != null && !emailResult.successful) || 
            (mobileResult != null && !mobileResult.successful) || 
            !passwordResult.successful) {
            _state.update {
                it.copy(
                    emailError = emailResult?.errorMessage,
                    mobileError = mobileResult?.errorMessage,
                    passwordError = passwordResult.errorMessage
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val result = registerUseCase(
                email = if (s.isOtpMode) null else s.email,
                mobile = if (s.isOtpMode) s.mobile else null,
                password = s.password
            )

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
        val emailResult = if (!s.isOtpMode) validateEmail(s.email) else null
        val mobileResult = if (s.isOtpMode) validateMobile(s.mobile) else null

        if ((emailResult != null && !emailResult.successful) || (mobileResult != null && !mobileResult.successful)) {
            _state.update {
                it.copy(
                    emailError = emailResult?.errorMessage,
                    mobileError = mobileResult?.errorMessage
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val result = forgotPasswordUseCase(
                email = if (s.isOtpMode) null else s.email,
                mobile = if (s.isOtpMode) s.mobile else null
            )

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
        val mobileResult = if (s.isOtpMode) validateMobile(s.mobile) else null
        val passwordResult = validatePassword(s.newPassword)
        
        if ((mobileResult != null && !mobileResult.successful) || !passwordResult.successful) {
            _state.update { 
                it.copy(
                    mobileError = mobileResult?.errorMessage,
                    newPasswordError = passwordResult.errorMessage 
                ) 
            }
            return
        }

        if (s.newPassword != s.confirmPassword) {
            _state.update { it.copy(confirmPasswordError = "PASSWORDS_DO_NOT_MATCH") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val result = if (s.isOtpMode) {
                resetPasswordWithOtpUseCase(s.mobile, s.otp, s.newPassword)
            } else {
                resetPasswordUseCase(s.token, s.newPassword)
            }

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
    val mobile: String = "",
    val password: String = "",
    val otp: String = "",

    val newPassword: String = "",
    val confirmPassword: String = "",
    val token: String = "",

    val emailError: Any? = null,
    val mobileError: Any? = null,
    val passwordError: Any? = null,
    val otpError: Any? = null,
    val newPasswordError: Any? = null,
    val confirmPasswordError: Any? = null,

    val isLoading: Boolean = false,
    val isOtpMode: Boolean = false,
    val otpSent: Boolean = false,
    val resendTimer: Int = 0
)

sealed class AuthIntent {
    data class OnEmailChange(val value: String) : AuthIntent()
    data class OnMobileChange(val value: String) : AuthIntent()
    data class OnPasswordChange(val value: String) : AuthIntent()
    data class OnOtpChange(val value: String) : AuthIntent()

    data class OnNewPasswordChange(val value: String) : AuthIntent()
    data class OnConfirmPasswordChange(val value: String) : AuthIntent()
    data class OnTokenReceived(val value: String) : AuthIntent()

    object ToggleAuthMode : AuthIntent()
    object SubmitLogin : AuthIntent()
    object ResendOtp : AuthIntent()
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
