package com.kazemieh.auth

import com.kazemieh.auth.screen.AuthViewModel
import com.kazemieh.domain.usecase.ForgotPasswordUseCase
import com.kazemieh.domain.usecase.LoginUseCase
import com.kazemieh.domain.usecase.RegisterUseCase
import com.kazemieh.domain.usecase.ResetPasswordUseCase
import com.kazemieh.domain.validation.ValidateEmail
import com.kazemieh.domain.validation.ValidatePassword
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
            validateEmail = get(),
            validatePassword = get()
        )
    }

    // UseCases
    factory { LoginUseCase(get()) }
    factory { RegisterUseCase(get()) }
    factory { ForgotPasswordUseCase(get()) }
    factory { ResetPasswordUseCase(get()) }

    // Validation
    factory { ValidateEmail() }
    factory { ValidatePassword() }
}