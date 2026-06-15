package com.kazemieh.auth

import com.kazemieh.auth.screen.AuthViewModel
import com.kazemieh.domain.auth.*
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {

    // ViewModel
    viewModel {
        AuthViewModel(
            loginUseCase = get(),
            registerUseCase = get(),
            forgotPasswordUseCase = get(),
            resetPasswordUseCase = get(),
            sendLoginOtpUseCase = get(),
            loginWithOtpUseCase = get(),
            resetPasswordWithOtpUseCase = get(),
            validateEmail = get(),
            validatePassword = get(),
            validateMobile = get(),
            validateUsername = get()
        )
    }

    // UseCases
    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
    factory { ForgotPasswordUseCase(get()) }
    factory { ResetPasswordUseCase(get()) }
    factory { SendLoginOtpUseCase(get()) }
    factory { LoginWithOtpUseCase(get()) }
    factory { ResetPasswordWithOtpUseCase(get()) }

    // Validation
    factory { ValidateEmail() }
    factory { ValidatePassword() }
    factory { ValidateMobile() }
    factory<ValidateUsername> { ValidateUsername() }
}
